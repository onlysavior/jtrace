package com.github.onlysavior.jtrace.analyse.offline;

import com.github.onlysavior.jtrace.core.StringUtils;
import com.github.onlysavior.jtrace.store.HbaseStoreProvider;
import com.github.onlysavior.jtrace.store.StoreProviders;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-10
 * Time: 下午7:45
 * To change this template use File | Settings | File Templates.
 */
public class Reducer2 extends MapReduceBase implements
        org.apache.hadoop.mapred.Reducer<Text, LongWritable, Text, LongWritable> {
    private static final HbaseStoreProvider hbase = StoreProviders.hbase();

    @Override
    public void reduce(Text text,
                       Iterator<LongWritable> longWritableIterator,
                       OutputCollector<Text, LongWritable> outputCollector,
                       Reporter reporter) throws IOException {
        long count = new LongWritable(0L).get();
        while (longWritableIterator.hasNext()) {
            count += longWritableIterator.next().get();
        }
        hbase.storeInPath(text.toString(), count);
        String revertPath = revert(text.toString());
        hbase.storeOutPath(revertPath, count);
        outputCollector.collect(text, new LongWritable(count));
        outputCollector.collect(new Text(revertPath), new LongWritable(count));
    }

    private String revert(String path) {
        if (!StringUtils.isNotBlank(path)) {
            return path;
        }
        String[] parts = path.split("|");
        StringBuilder sb = new StringBuilder();
        sb.append(parts[parts.length - 1]);
        for (int index = parts.length - 2; index > 1; index--) {
            sb.append("|");
            sb.append(parts[index]);
        }
        return sb.toString();
    }
}
