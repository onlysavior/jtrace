package com.github.onlysavior.jtrace.core.test;

import com.github.onlysavior.jtrace.core.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-12
 * Time: 上午10:49
 * To change this template use File | Settings | File Templates.
 */
public class TraceLogTest {

    @Test
    public void testLog() throws InterruptedException {

        Jtrace.startTrace(TraceIdGen.generate(), "http://www.taobao.com");
        Thread.sleep(100);
        Jtrace.startRpc("tddl","openConnection");
        Jtrace.rpcClientSend();
        Thread.sleep(20);
        Jtrace.rpcServerRecv("127.0.0.1", "tddl", "openConnection");
        Thread.sleep(50);
        Jtrace.rpcServerSend();
        Thread.sleep(20);
        Jtrace.rpcClientRecv(Jtrace.RPC_RESULT_SUCCESS, 1);
        Thread.sleep(20);
        Jtrace.endTrace(Jtrace.RPC_RESULT_SUCCESS, 2);
        Jtrace.flush();
    }
}
