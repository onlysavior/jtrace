package com.github.onlysavior.jtrace.analyse.offline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-9
 * Time: 下午6:02
 * To change this template use File | Settings | File Templates.
 */
public class CallPath {
    private CallNode root;
    private String entry;

    public CallNode getRoot() {
        return root;
    }

    public void setRoot(CallNode root) {
        this.root = root;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public static class CallNode {
        private CallNode parent;
        private List<CallNode> children = new ArrayList<CallNode>();

        private String traceId;
        private int rpcType;
        private String traceName;
        private long entrySign;
        private long nodeSign;
        private long startTime;
        private long last;
        private String remoteIp;

        private String serverName;
        private String methodName;
        private String spanId;
        private String parentSpanId;

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public int getRpcType() {
            return rpcType;
        }

        public void setRpcType(int rpcType) {
            this.rpcType = rpcType;
        }

        public String getTraceName() {
            return traceName;
        }

        public void setTraceName(String traceName) {
            this.traceName = traceName;
        }

        public long getEntrySign() {
            return entrySign;
        }

        public void setEntrySign(long entrySign) {
            this.entrySign = entrySign;
        }

        public long getNodeSign() {
            return nodeSign;
        }

        public void setNodeSign(long nodeSign) {
            this.nodeSign = nodeSign;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getLast() {
            return last;
        }

        public void setLast(long last) {
            this.last = last;
        }

        public String getRemoteIp() {
            return remoteIp;
        }

        public void setRemoteIp(String remoteIp) {
            this.remoteIp = remoteIp;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getSpanId() {
            return spanId;
        }

        public void setSpanId(String spanId) {
            this.spanId = spanId;
        }

        public String getParentSpanId() {
            return parentSpanId;
        }

        public void setParentSpanId(String parentSpanId) {
            this.parentSpanId = parentSpanId;
        }
    }
}
