package org.jboss.windup.web.services;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

/**
 * This contains useful utility methods for the services module.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ServiceUtil
{
    private static final Logger LOG = Logger.getLogger(ServiceUtil.class.getName());

    /**
     * Lookup a JMS Queue.
     */
    public static Queue getJMSQueue(String jndiName)
    {
        return lookup("JMSQueue", Queue.class, jndiName);
    }

    public static void sendJMSMessage(Destination destination, Serializable serializable)
    {
        try (Connection messaging = getJMSContext())
        {
            try (Session session = messaging.createSession(true, Session.AUTO_ACKNOWLEDGE))
            {
                Message message = session.createObjectMessage(serializable);
                session.createProducer(destination).send(message);
            }
            LOG.info("Message sent: " + serializable);
        }
        catch (Exception e)
        {
            LOG.log(Level.SEVERE, "Error rolling back message send due to: " + e.getMessage(), e);
        }
    }

    /**
     * Lookup the JMSContext from JNDI.
     */
    public static Connection getJMSContext()
    {
        ConnectionFactory connectionFactory = lookup("JMSContext", ConnectionFactory.class, "java:/ConnectionFactory");
        try
        {
            return connectionFactory.createConnection();
        }
        catch (JMSException e)
        {
            throw new RuntimeException("Connection to JMS Failed due to: " + e.getMessage());
        }
    }

    /**
     * Lookup the {@link UserTransaction} manually from the container.
     */
    public static UserTransaction getUserTransaction()
    {
        return lookup("UserTransaction", UserTransaction.class, "java:jboss/UserTransaction");
    }

    private static <T> T lookup(String description, Class<T> clazz, String jndiName)
    {
        try
        {
            // Begin of task
            InitialContext ctx = new InitialContext();
            return (T) ctx.lookup(jndiName);
        }
        catch (NamingException e)
        {
            throw new RuntimeException("Failed to lookup " + description + " due to: " + e.getMessage());
        }
    }
}
