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

import org.jboss.windup.web.services.model.RegisteredApplication;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
            @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
            @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/" + MessagingConstants.PACKAGE_DISCOVERY_QUEUE),
})
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PackageDiscoveryMDB extends AbstractMDB implements MessageListener
{
    private static Logger LOG = Logger.getLogger(PackageDiscoveryMDB.class.getName());

    @Inject
    private Instance<PackageDiscoveryTask> packageDiscoveryTaskInstance;

    @Override
    public void onMessage(Message message)
    {
        LOG.info("1Received package discovery request: " + message);
        if (!validatePayload(RegisteredApplication.class, message))
            return;

        try
        {
            LOG.info("Processing package discovery request: " + ((ObjectMessage)message).getObject());
            RegisteredApplication application = (RegisteredApplication) ((ObjectMessage) message).getObject();

            PackageDiscoveryTask executionTask = packageDiscoveryTaskInstance.get();
            executionTask.setApplication(application);
            executionTask.run();
        }
        catch (Throwable e)
        {
            LOG.log(Level.SEVERE, "Failed to execute package discovery due to: " + e.getMessage(), e);
        }
    }
}
