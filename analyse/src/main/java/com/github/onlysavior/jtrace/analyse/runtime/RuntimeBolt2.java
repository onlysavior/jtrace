package com.github.onlysavior.jtrace.analyse.runtime;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.github.onlysavior.jtrace.store.HbaseStoreProvider;
import com.github.onlysavior.jtrace.store.StoreProviders;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-11
 * Time: 下午9:50
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeBolt2 extends BaseRichBolt {
    HbaseStoreProvider hbaseStoreProvider;
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        hbaseStoreProvider = StoreProviders.hbase();
    }

    @Override
    public void execute(Tuple tuple) {
        Long entrySign = tuple.getLongByField("entrySign");
        Long nodeSign = tuple.getLongByField("entrySign");
        Long rt =  tuple.getLongByField("rt");
        String rowKey = hbaseStoreProvider.locate(entrySign, nodeSign);
        hbaseStoreProvider.updateRT(rowKey, rt);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }
}
