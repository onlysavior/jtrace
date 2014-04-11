package com.github.onlysavior.jtrace.analyse.runtime;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.github.onlysavior.jtrace.store.HdfsStoreProvider;
import com.github.onlysavior.jtrace.store.StoreProviders;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-11
 * Time: 下午9:25
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeBolt1 extends BaseRichBolt {
    HdfsStoreProvider hdfsStoreProvider;
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        hdfsStoreProvider = StoreProviders.hdfs();
    }

    @Override
    public void execute(Tuple tuple) {
        String traceId = tuple.getString(0);
        String data = tuple.getString(1);
        hdfsStoreProvider.store(traceId, data);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }
}
