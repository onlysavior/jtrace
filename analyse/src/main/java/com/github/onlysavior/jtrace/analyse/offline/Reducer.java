package com.github.onlysavior.jtrace.analyse.offline;

import com.github.onlysavior.jtrace.store.HbaseStoreProvider;
import com.github.onlysavior.jtrace.store.StoreProviders;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-9
 * Time: 下午3:07
 * To change this template use File | Settings | File Templates.
 */
public class Reducer extends MapReduceBase implements
        org.apache.hadoop.mapred.Reducer<Text, Text, Void, Void> {
    private static final HbaseStoreProvider store = StoreProviders.hbase();

    @Override
    public void reduce(Text text,
                       Iterator<Text> textIterator,
                       OutputCollector<Void, Void> voidVoidOutputCollector,
                       Reporter reporter) throws IOException {

    }
}
