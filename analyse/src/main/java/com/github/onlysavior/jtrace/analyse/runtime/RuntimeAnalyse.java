package com.github.onlysavior.jtrace.analyse.runtime;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import com.github.onlysavior.jtrace.analyse.AnalyseException;
import com.github.onlysavior.jtrace.store.LifeCycleSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-11
 * Time: 下午3:59
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeAnalyse extends LifeCycleSupport {
    @Override
    public void start() {
        super.start();
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new RuntimeSpout(), 1);
        builder.setBolt("hdfs", new RuntimeBolt1(), 1).shuffleGrouping("spout");
        builder.setBolt("count", new RuntimeBolt2(), 2).shuffleGrouping("spout");

        Config conf = new Config();
        conf.setNumWorkers(3);
        try {
            StormSubmitter.submitTopology("runtimeAnalyse", conf, builder.createTopology());
        } catch (AlreadyAliveException ignore) {
        } catch (InvalidTopologyException e) {
            throw new AnalyseException(e);
        }
    }
}
