package com.github.onlysavior.jtrace.analyse.runtime;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.github.onlysavior.jtrace.analyse.AnalyseException;
import com.github.onlysavior.jtrace.core.StringUtils;
import com.github.onlysavior.jtrace.core.TraceUtils;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-11
 * Time: 下午8:43
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeSpout extends BaseRichSpout {
    SpoutOutputCollector collector;
    ConnectionFactory connectionFactory;
    Connection connection;
    Session session;
    MessageConsumer consumer;

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        collector = spoutOutputCollector;
        connect();
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (JMSException e) {
            throw new AnalyseException(e);
        }
        super.close();
    }

    @Override
    public void nextTuple() {
        try {
            TextMessage message = (TextMessage) consumer.receive(3000);
            String data = message.getText();
            if (StringUtils.isNotBlank(data)) {
                TraceUtils helper = new TraceUtils(data);
                collector.emit(new Values(helper.getTraceId(),
                        helper.getData(), helper.getEntrySign(), helper.getNodeSign(),
                        helper.getRT()));
            }
        } catch (JMSException e) {
            connect();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("traceId","data","entrySign","nodeSign","rt"));
    }

    private void connect() {
        try {
            connectionFactory = new ActiveMQConnectionFactory(
                    ActiveMQConnection.DEFAULT_USER,
                    ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("jtrace");
            consumer = session.createConsumer(destination);
        } catch (JMSException e) {
            throw new AnalyseException(e);
        }
    }
}
