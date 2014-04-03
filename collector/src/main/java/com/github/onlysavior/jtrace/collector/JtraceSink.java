package com.github.onlysavior.jtrace.collector;

import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-3
 * Time: 下午1:56
 * To change this template use File | Settings | File Templates.
 */
public class JtraceSink extends AbstractSink implements Configurable {
    @Override
    public Status process() throws EventDeliveryException {
        return null;
    }


    @Override
    public void configure(Context context) {

    }
}
