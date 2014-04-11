package com.github.onlysavior.jtrace.analyse.offline;

import java.util.LinkedHashSet;
import java.util.Set;

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
        private Set<CallNode> children = new LinkedHashSet<CallNode>();

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
        private String path;
        private String[] pathC;
        private int pathIndex = 0;

        public CallNode getParent() {
            return parent;
        }

        public void setParent(CallNode parent) {
            this.parent = parent;
        }

        public Set<CallNode> getChildren() {
            return children;
        }

        public void addChild(CallNode child) {
            this.children.add(child);
        }

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

        public String getPath() {
            if (pathIndex == 0) {
                return path;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(pathC[pathIndex]);
            for (int index = pathIndex + 1; pathIndex < pathC.length; index++) {
                sb.append("|");
                sb.append(pathC[index]);
            }
            return sb.toString();
        }

        public void setPath(String path) {
            this.path = path;
            this.pathC = path.split("|");
        }

        public boolean walk(int step) {
            assert step < 0 : "illeagl step";
            if (step > pathC.length) {
                return false;
            }
            pathIndex = step;
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CallNode)) return false;

            CallNode callNode = (CallNode) o;

            if (entrySign != callNode.entrySign) return false;
            if (last != callNode.last) return false;
            if (nodeSign != callNode.nodeSign) return false;
            if (rpcType != callNode.rpcType) return false;
            if (startTime != callNode.startTime) return false;
            if (!children.equals(callNode.children)) return false;
            if (methodName != null ? !methodName.equals(callNode.methodName) : callNode.methodName != null)
                return false;
            if (parent != null ? !parent.equals(callNode.parent) : callNode.parent != null) return false;
            if (parentSpanId != null ? !parentSpanId.equals(callNode.parentSpanId) : callNode.parentSpanId != null)
                return false;
            if (remoteIp != null ? !remoteIp.equals(callNode.remoteIp) : callNode.remoteIp != null) return false;
            if (serverName != null ? !serverName.equals(callNode.serverName) : callNode.serverName != null)
                return false;
            if (spanId != null ? !spanId.equals(callNode.spanId) : callNode.spanId != null) return false;
            if (traceId != null ? !traceId.equals(callNode.traceId) : callNode.traceId != null) return false;
            if (traceName != null ? !traceName.equals(callNode.traceName) : callNode.traceName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + children.hashCode();
            result = 31 * result + (traceId != null ? traceId.hashCode() : 0);
            result = 31 * result + rpcType;
            result = 31 * result + (traceName != null ? traceName.hashCode() : 0);
            result = 31 * result + (int) (entrySign ^ (entrySign >>> 32));
            result = 31 * result + (int) (nodeSign ^ (nodeSign >>> 32));
            result = 31 * result + (int) (startTime ^ (startTime >>> 32));
            result = 31 * result + (int) (last ^ (last >>> 32));
            result = 31 * result + (remoteIp != null ? remoteIp.hashCode() : 0);
            result = 31 * result + (serverName != null ? serverName.hashCode() : 0);
            result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
            result = 31 * result + (spanId != null ? spanId.hashCode() : 0);
            result = 31 * result + (parentSpanId != null ? parentSpanId.hashCode() : 0);
            return result;
        }
    }
}
