package org.jboss.windup.web.services.messaging;

import java.util.GregorianCalendar;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.ejb3.annotation.Pool;
import org.jboss.windup.web.addons.websupport.services.ProjectLoaderService;
import org.jboss.windup.web.addons.websupport.services.WindupExecutorService;
import org.jboss.windup.web.furnaceserviceprovider.FromFurnace;
import org.jboss.windup.web.services.model.AnalysisContext;
import org.jboss.windup.web.services.model.ExecutionState;
import org.jboss.windup.web.services.model.RegisteredApplication;
import org.jboss.windup.web.services.model.WindupExecution;
import org.jboss.windup.web.services.service.DefaultGraphPathLookup;
import org.jboss.windup.web.services.service.WindupExecutionService;
import org.jboss.windup.web.services.websocket.WSJMSMessage;

/**
 * This receives updates from the Windup execution backend processes and persists the current state to the database.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
            @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1"),
            @ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "1"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/" + MessagingConstants.STATUS_UPDATE_QUEUE),
})
@Pool(value = "mdb-strict-max-pool")
public class StatusUpdateMDB extends AbstractMDB implements MessageListener
{
    private static Logger LOG = Logger.getLogger(StatusUpdateMDB.class.getName());

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @FromFurnace
    private WindupExecutorService windupExecutorService;

    @Inject
    @FromFurnace
    private ProjectLoaderService projectLoaderService;

    @Inject
    private WindupExecutionService windupExecutionService;

    /**
     * Event sent to WebSocket ExecutionProgressReporter
     */
    @Inject
    @WSJMSMessage
    Event<Message> informWebSocketEvent;

    @PostConstruct
    protected void initialize()
    {
        this.projectLoaderService.setGraphPathLookup(new DefaultGraphPathLookup(this.entityManager));
    }

    @Override
    public void onMessage(Message message)
    {
        LOG.info("1Received execution update event: " + message);

        // Make sure that we are receiving the correct type of message
        if (!validatePayload(WindupExecution.class, message))
            return;

        try
        {
            WindupExecution execution = (WindupExecution) ((ObjectMessage) message).getObject();
            LOG.info("Received execution update event: " + execution);

            // Update the DB with this information
            WindupExecution fromDB = entityManager.find(WindupExecution.class, execution.getId(), LockModeType.PESSIMISTIC_WRITE);

            if (fromDB == null)
            {
                LOG.warning("Received unrecognized status update for execution: " + fromDB);
                return;
            }

            if (execution.getState() != ExecutionState.CANCELLED && fromDB.getState() == ExecutionState.CANCELLED)
            {
                // If the update from the engine hasn't been cancelled, try to send another request
                this.windupExecutionService.cancelExecution(fromDB.getId());
            }

            if (fromDB.getState() == ExecutionState.CANCELLED)
            {
                LOG.warning("Not continuing to update state for cancelled status...");
                fromDB.setTimeCompleted(new GregorianCalendar());
                return;
            }

            fromDB.setLastModified(execution.getLastModified());
            fromDB.setTimeStarted(execution.getTimeStarted());

            fromDB.setTimeCompleted(execution.getTimeCompleted());

            fromDB.setTotalWork(execution.getTotalWork());
            fromDB.setWorkCompleted(execution.getWorkCompleted());
            fromDB.setCurrentTask(execution.getCurrentTask());

            fromDB.setApplicationListRelativePath(execution.getApplicationListRelativePath());
            fromDB.setOutputPath(execution.getOutputPath());
            fromDB.setState(execution.getState());

            // Once the run is complete, make sure that we have the correct path information in the execution.
            if (fromDB.getState() == ExecutionState.COMPLETED)
            {
                removeDeletedApplications(fromDB);
            }

            informWebSocketEvent.fire(message);
        }
        catch (Throwable e)
        {
            LOG.log(Level.SEVERE, "Failed to update status information due to: " + e.getMessage(), e);
        }
    }

    private void removeDeletedApplications(WindupExecution execution)
                throws HeuristicMixedException, HeuristicRollbackException,
                NamingException, NotSupportedException, RollbackException, SystemException
    {
        AnalysisContext context = execution.getAnalysisContext();

        Set<RegisteredApplication> applications = context.getApplications();
        applications.removeIf((RegisteredApplication app) -> app.isDeleted());
    }
}
