package com.github.onlysavior.jtrace.analyse.offline;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-10
 * Time: 下午7:37
 * To change this template use File | Settings | File Templates.
 */
public class Map2 extends MapReduceBase implements
        org.apache.hadoop.mapred.Mapper<Text,LongWritable,Text,LongWritable> {
    @Override
    public void map(Text text, LongWritable longWritable, OutputCollector<Text, LongWritable> outputCollector, Reporter reporter) throws IOException {
        outputCollector.collect(text, longWritable);
    }
}
