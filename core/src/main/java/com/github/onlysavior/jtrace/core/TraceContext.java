package com.github.onlysavior.jtrace.core;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 下午12:27
 * To change this template use File | Settings | File Templates.
 */
public final class TraceContext implements Serializable {

    private static final ThreadLocal<TraceContext> local = new ThreadLocal<TraceContext>();

    String traceId;
    String parentSpanId;
    String spanId;

    String traceName = StringUtils.EMPTY_STRING;
    String serverName = StringUtils.EMPTY_STRING;
    String methodName = StringUtils.EMPTY_STRING;
    String remoteIp = StringUtils.EMPTY_STRING;

    int logType;
    int rpcType;
    int span0;
    int spna1;
    long startTime = 0L;
    long endTime = 0L;

    long entrySign = 0L;
    long nodeSign = 0L;

    TraceContext parentTraceContext;
    private AtomicInteger rpcCounter = new AtomicInteger(0);

    public TraceContext() {
        //noop for ringBuffer preallocate
    }

    public TraceContext(int logType) {
        this(StringUtils.EMPTY_STRING);
        this.logType = logType;
    }

    public TraceContext(String traceId) {
        this(traceId, Jtrace.ROOT_PARENT_SPAN_ID, Jtrace.ROOT_SPAN_ID);
    }

    public TraceContext(String traceId, String parentSpanId, String spanId) {
        this(traceId, parentSpanId, spanId, null);
    }

    public TraceContext(String traceId, String parentSpanId, String spanId, TraceContext parentTraceContext) {
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        if (parentTraceContext != null) {
            this.parentTraceContext = parentTraceContext;
            this.entrySign = parentTraceContext.entrySign;
        }
    }

    public TraceContext createChildContext() {
        TraceContext child = new TraceContext(traceId, spanId, nextSpanId(), this);
        return child;
    }

    public void copy(TraceContext other) {
        this.parentTraceContext = other.parentTraceContext;
        this.endTime = other.endTime;
        this.rpcType = other.rpcType;
        this.logType = other.logType;
        this.methodName = other.methodName;
        this.remoteIp = other.remoteIp;
        this.parentSpanId = other.parentSpanId;
        this.serverName = other.serverName;
        this.startTime = other.startTime;
        this.span0 = other.span0;
        this.spanId = other.spanId;
        this.spna1 = other.spna1;
        this.traceName = other.traceName;
        this.traceId = other.traceId;
        this.entrySign = other.entrySign;
        this.nodeSign = other.nodeSign;
    }

    String nextSpanId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public void startTrace(String traceName) {
        this.traceName = traceName;
        this.entrySign = traceName.hashCode();
        this.startTime = System.currentTimeMillis();
    }

    public void endTrace(String serverName, int rpcType) {
        this.logType = Jtrace.LOG_TYPE_TRACE_END;
        this.endTime = System.currentTimeMillis();
        this.rpcType = rpcType;
    }

    public void startRpc(String serverName, String methodName) {
        this.serverName = serverName;
        this.methodName = methodName;
        this.startTime = System.currentTimeMillis();
        this.span0 = 0;
    }

    public void endRpc(String result, int type) {
        this.logType = Jtrace.LOG_TYPE_RPC_END;
        this.endTime = System.currentTimeMillis();
        this.rpcType = type;
        this.spna1 = (int) (this.endTime - this.startTime);
    }

    public void rpcClientSend() {
        this.span0 = (int) (System.currentTimeMillis() - this.startTime);
    }

    public void rpcServerRecv(String serverName, String methodName, long sign) {
        this.startTime = System.currentTimeMillis();
        this.serverName = serverName;
        this.methodName = methodName;
        this.nodeSign = 31*sign + serverName.hashCode();
    }

    public void rpcServerSend(int type) {
        this.logType = Jtrace.LOG_TYPE_SERVER_SEND;
        this.rpcType = type;
        this.endTime = System.currentTimeMillis();
    }

    public static TraceContext get() {
        return local.get();
    }

    public static void set(TraceContext context) {
        local.set(context);
    }
}
