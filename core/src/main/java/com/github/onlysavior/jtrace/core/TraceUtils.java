package com.github.onlysavior.jtrace.core;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-11
 * Time: 下午9:11
 * To change this template use File | Settings | File Templates.
 */
public class TraceUtils {
    private String originData;
    private int type;
    private String[] dataC;

    public TraceUtils(String s) {
        this.originData = s;
        dataC = originData.split("|");
        type = Integer.valueOf(dataC[2]);
    }

    public String getData() {
        return originData;
    }

    public String getTraceId() {
        return dataC[0];
    }

    public int getType() {
        return type;
    }

    public String getStartTime() {
        return dataC[1];
    }

    public String getServerName() {
        String serverName = serverName();
        String traceName = traceName();

        return StringUtils.isNotBlank(serverName) ? serverName :
                StringUtils.isNotBlank(traceName) ? traceName : null;
    }

    public long getEntrySign() {
        switch (type) {
            case Jtrace.LOG_TYPE_TRACE_END:
                return Long.parseLong(dataC[7]);
            case Jtrace.LOG_TYPE_SERVER_SEND:
                return Long.parseLong(dataC[8]);
            case Jtrace.LOG_TYPE_RPC_END:
                return Long.parseLong(dataC[11]);
            default:
                return 0L;
        }
    }

    public long getNodeSign() {
        switch (type) {
            case Jtrace.LOG_TYPE_TRACE_END:
                return Long.parseLong(dataC[8]);
            case Jtrace.LOG_TYPE_SERVER_SEND:
                return Long.parseLong(dataC[9]);
            case Jtrace.LOG_TYPE_RPC_END:
                return Long.parseLong(dataC[12]);
            default:
                return 0L;
        }
    }

    public long getRT() {
        switch (type) {
            case Jtrace.LOG_TYPE_TRACE_END:
                return Long.parseLong(dataC[5]);
            case Jtrace.LOG_TYPE_RPC_END:
                String s = dataC[9];
                return parseDuring(s);
            case Jtrace.LOG_TYPE_SERVER_SEND:
                return Long.parseLong(dataC[4]);
            default:
                return 0L;
        }
    }

    private Long parseDuring(String spans) {
        String removed = spans.substring(1, spans.length() - 1);
        String[] durings = removed.split(",");
        return Long.valueOf(Integer.parseInt(durings[durings.length - 1]) - Integer.parseInt(durings[0]));
    }

    private String serverName() {
        switch (type) {
            case Jtrace.LOG_TYPE_TRACE_END:
                if (dataC.length > 10) {
                    return dataC[10];
                }
            case Jtrace.LOG_TYPE_RPC_END:
                return dataC[6];
            case Jtrace.LOG_TYPE_SERVER_SEND:
                if (dataC.length > 10) {
                    return dataC[10];
                }
            default:
                return null;
        }
    }

    private String traceName() {
        switch (type) {
            case Jtrace.LOG_TYPE_TRACE_END:
                return dataC[7];
            case Jtrace.LOG_TYPE_RPC_END:
                return dataC[10];
            case Jtrace.LOG_TYPE_SERVER_SEND:
            default:
                return null;
        }
    }
}
