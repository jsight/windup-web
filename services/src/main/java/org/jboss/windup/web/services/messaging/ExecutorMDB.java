package org.jboss.windup.web.services.messaging;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.ejb3.annotation.Pool;
import org.jboss.windup.web.services.model.WindupExecution;

/**
 * This receives requests to run Windup and executes them.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1"),
    @ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "1"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/" + MessagingConstants.EXECUTOR_QUEUE)
})
@Pool(value = "mdb-strict-max-pool")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ExecutorMDB extends AbstractMDB implements MessageListener
{
    private static Logger LOG = Logger.getLogger(ExecutorMDB.class.getName());

    @Inject
    private Instance<WindupExecutionTask> windupExecutionTaskInstance;

    @Override
    public void onMessage(Message message)
    {
        LOG.info("Received execution request: " + message);
        if (!validatePayload(WindupExecution.class, message))
            return;

        try
        {
            WindupExecution execution = (WindupExecution)((ObjectMessage) message).getObject();
            if (ExecutionStateCache.isCancelled(execution))
            {
                LOG.info("Not executing " + execution.getId() + " as it has been marked cancelled!");
                return;
            }

            WindupExecutionTask executionTask = windupExecutionTaskInstance.get();
            executionTask.init(execution, execution.getAnalysisContext());
            executionTask.run();
        }
        catch (Throwable e)
        {
            LOG.log(Level.SEVERE, "Failed to execute windup due to: " + e.getMessage(), e);
        }
    }


}
