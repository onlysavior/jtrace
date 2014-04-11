package com.github.onlysavior.jtrace.collector;

import com.github.onlysavior.jtrace.core.Jtrace;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

import javax.jms.*;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-11
 * Time: 下午4:35
 * To change this template use File | Settings | File Templates.
 */
public class JtraceSink extends AbstractSink implements Configurable {
    ConnectionFactory connectionFactory;
    Connection connection = null;
    MessageProducer producer;
    Session session;


    @Override
    public synchronized void stop() {
        try {
            connection.stop();
        } catch (JMSException e) {
            throw new CollectorException(e);
        }
        super.stop();
    }

    @Override
    public synchronized void start() {
        super.start();
        try {
            connect();
        } catch (JMSException e) {
            throw new CollectorException(e);
        }
    }

    @Override
    public void configure(Context context) {
    }

    @Override
    public Status process() throws EventDeliveryException {
        Status status = Status.BACKOFF;
        final Channel channel = getChannel();
        final Transaction txn = channel.getTransaction();
        txn.begin();

        try {
            Event event = channel.take();
            if (event != null) {
                while ((event = channel.take()) != null) {
                    TextMessage message = session.createTextMessage(create(event));
                    producer.send(message);
                }
                status = Status.READY;
            }
            txn.commit();
        } catch (JMSException e) {
            txn.rollback();
            try {
                connect();
            } catch (JMSException ignore) {
            }
        } finally {
            txn.close();
        }
        return status;
    }

    private void connect() throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(Boolean.TRUE,
                Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("jtrace");
        producer = session.createProducer(destination);
    }

    private String create(Event event) {
        final byte[] body = event.getBody();
        return new String(body, Jtrace.DEFAULT_CHARACTER);
    }
}
