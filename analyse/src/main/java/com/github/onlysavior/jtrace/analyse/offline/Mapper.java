package com.github.onlysavior.jtrace.analyse.offline;

import com.github.onlysavior.jtrace.analyse.AnalyseException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-9
 * Time: 上午10:22
 * To change this template use File | Settings | File Templates.
 */
public class Mapper extends MapReduceBase implements
        org.apache.hadoop.mapred.Mapper<LongWritable, Text, Text, Text>{
    @Override
    public void map(LongWritable longWritable,
                    Text text,
                    OutputCollector<Text, Text> textTextOutputCollector,
                    Reporter reporter) throws IOException {
        if (text == null) {
            throw new AnalyseException("text is null");
        }
        String info = text.toString();
        if (info == null ||info.trim().length() < 25) {
            return;
        }
        String traceId = info.substring(0, 25);
        textTextOutputCollector.collect(new Text(traceId), text);
    }
}
