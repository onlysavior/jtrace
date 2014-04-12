package com.github.onlysavior.jtrace.core.test;

import com.github.onlysavior.jtrace.core.TraceIdGen;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-12
 * Time: 上午10:09
 * To change this template use File | Settings | File Templates.
 */
public class TraceIdGenTest {
    @Test
    public void testTraceIdGen() {
        Set<String> set = new HashSet<String>();
        for (int index = 0; index < 10; index++) {
            set.add(TraceIdGen.generate());
        }

        Assert.assertEquals(10, set.size());

        String single = TraceIdGen.generate();
        System.out.printf("single, %s", single);
        Assert.assertEquals(25, single.length());

        try {
            Long date = Long.parseLong(single.substring(8, 21));
            Date d = new Date(date);
            System.out.println(d);
        } catch (Throwable e) {
            Assert.fail();
        }
    }
}
