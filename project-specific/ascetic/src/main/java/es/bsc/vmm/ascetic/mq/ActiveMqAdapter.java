/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.vmm.ascetic.mq;

import es.bsc.vmm.core.configuration.VmmConfig;
import es.bsc.vmm.core.logging.VMMLogger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.jms.*;

public class ActiveMqAdapter {

    private final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
            VmmConfig.INSTANCE.getConfiguration().getString("activeMqUrl","tcp://localhost:61616")
    );

    private final Logger log = LogManager.getLogger(ActiveMqAdapter.class);
    private Connection connection;
    private Session session;

    /**
     * Publishes a message in the queue with the topic and the message specified
     *
     * @param topic the topic
     * @param message the message
     */
    public void publishMessage(String topic, String message) {
        Connection connection = null;
        Session session = null;
        try {
            // Create a Connection
            connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination
            Destination destination = session.createTopic(topic);

            // Create a MessageProducer from the Session to the Topic
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            producer.send(session.createTextMessage(message));

            session.close();
            connection.close();
        } catch (Exception e) {
            VMMLogger.logCouldNotSendToMessageQueue(topic);
        } finally {
            try {
                session.close();
                connection.close();
            } catch(Exception e) {
                log.warn("Can't close connection: " + e.getMessage());
            }
        }
    }

    // Hi Mario, the queue is iaas-slam.monitoring.<slaId>.<vmId>.violationNotified

//    public void subscribeToTopic
//
//    public void something() throws Exception {
//        TopicConnection topicConnection = connectionFactory.createTopicConnection();
//        TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
////        topicSession.
//
//    }

}
