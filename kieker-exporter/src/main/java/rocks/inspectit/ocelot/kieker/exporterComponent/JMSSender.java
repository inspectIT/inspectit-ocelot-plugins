package rocks.inspectit.ocelot.kieker.exporterComponent;

import kieker.common.record.IMonitoringRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Creates a connection to ApacheMQ
 */
@Slf4j
public class JMSSender {
    private static String connectionFactoryUrl;
    private static String queueName;

    private static Session session;
    private static MessageProducer producer;
    private static Connection connection;
    private static Destination destination;

    public JMSSender(String connectionUrl, String queue) throws JMSException {
        connectionFactoryUrl = connectionUrl;
        queueName = queue;
        init();
    }

    private static void init() throws JMSException {
        try {
            //Create a ConnectionFactory + Connection
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionFactoryUrl);
            connection = connectionFactory.createConnection();
            connection.start();

            //Create Session + destination
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(queueName);

            // Create a MessageProducer from the Session to the Topic or Queue
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (Exception e) {
            log.error("Error creating JMS Sender");
            e.printStackTrace();
            throw new JMSException("JMS Sender to " + connectionFactoryUrl + " with queue: " + queueName + " could not be created.");
        }
    }

    public static void send(final IMonitoringRecord record) {
        try {
            // Create a messages and tell the producer to send the message
            final Message msg = session.createObjectMessage(record);
            producer.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}