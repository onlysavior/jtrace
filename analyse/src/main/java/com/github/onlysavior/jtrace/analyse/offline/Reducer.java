package com.github.onlysavior.jtrace.analyse.offline;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-9
 * Time: 下午3:07
 * To change this template use File | Settings | File Templates.
 */
public class Reducer extends MapReduceBase implements
        org.apache.hadoop.mapred.Reducer<Text, Text, Text, LongWritable> {
    private static final CallNodeParser parser = new CallNodeParser();
    private static final CallPathMaker  pathMaker = new CallPathMaker();
    private static final LongWritable one = new LongWritable(1L);

    @Override
    public void reduce(Text text,
                       Iterator<Text> textIterator,
                       OutputCollector<Text, LongWritable> outputCollector,
                       Reporter reporter) throws IOException {
        List<CallPath.CallNode> originNodes = new ArrayList<CallPath.CallNode>(512);
        while (textIterator.hasNext()) {
            CallPath.CallNode node = parser.parse(textIterator.next().toString());
            if (node != null) {
                originNodes.add(node);
            }
        }
        CallPath.CallNode root = pathMaker.make(originNodes);
        if (root != null) {
            walk(root, outputCollector);
        }
    }

    private void walk(CallPath.CallNode node, OutputCollector<Text, LongWritable> outputCollector)
            throws IOException {
        outputCollector.collect(new Text(node.getPath()), one);
        for (CallPath.CallNode n : node.getChildren()) {
            walk(n, outputCollector);
        }

        boolean hasNext;
        do {
            int step = 1;
            hasNext = node.walk(++step);
            outputCollector.collect(new Text(node.getPath()), one);
        } while (hasNext);
    }
}
