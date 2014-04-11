package com.github.onlysavior.jtrace.analyse.offline;

import com.github.onlysavior.jtrace.analyse.AnalyseException;
import com.github.onlysavior.jtrace.core.Jtrace;
import com.github.onlysavior.jtrace.core.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-9
 * Time: 下午6:56
 * To change this template use File | Settings | File Templates.
 */
public class CallNodeParser {
    public CallPath.CallNode parse(String info) {
        if (!StringUtils.isNotBlank(info)) {
            throw new AnalyseException("info is null");
        }
        String[] parts = info.split("|");
        if (parts == null || parts.length < 3) {
            return null;
        }

        Integer rpcType = Integer.valueOf(parts[2]);
        CallPath.CallNode node = null;
        switch (rpcType) {
            case Jtrace.LOG_TYPE_TRACE_END:
                node = new CallPath.CallNode();
                parseTraceEnd(node, parts);
                break;
            case Jtrace.LOG_TYPE_RPC_END:
                node = new CallPath.CallNode();
                parseRpcEnd(node, parts);
                break;
            case Jtrace.LOG_TYPE_SERVER_SEND:
                node = new CallPath.CallNode();
                parseServerSend(node, parts);
                break;
            default:
                node = null;

        }
        return node;
    }

    private void parseServerSend(CallPath.CallNode node, String[] parts) {
        if (parts.length < 10) {
            throw new AnalyseException("invalid format");
        }
        node.setTraceId(parts[0]);
        node.setStartTime(Long.parseLong(parts[1]));
        node.setRpcType(Integer.parseInt(parts[3]));
    }

    private CallPath.CallNode parseTraceEnd(CallPath.CallNode node, String[] parts) {
        if (parts.length < 11) {
            throw new AnalyseException("invalid format");
        }
        node.setTraceId(parts[0]);
        node.setStartTime(Long.parseLong(parts[1]));
        node.setRpcType(Integer.parseInt(parts[3]));
        node.setSpanId(parts[4]);
        node.setLast(Long.parseLong(parts[5]));
        node.setParentSpanId(parts[6]);
        node.setSpanId(parts[7]);
        node.setRemoteIp(parts[8]);
        node.setEntrySign(Long.parseLong(parts[9]));
        node.setNodeSign(Long.parseLong(parts[10]));

        if (parts.length > 11) {
            node.setServerName(parts[11]);
            if (parts.length >= 13) {
                node.setMethodName(parts[12]);
            }
        }
        return node;
    }

    private CallPath.CallNode parseRpcEnd(CallPath.CallNode node, String[] parts) {
        if (parts.length < 12) {
            throw new AnalyseException("invalid format");
        }
        node.setTraceId(parts[0]);
        node.setStartTime(Long.parseLong(parts[1]));
        node.setRpcType(Integer.parseInt(parts[3]));
        node.setParentSpanId(parts[4]);
        node.setSpanId(parts[5]);
        node.setServerName(parts[6]);
        node.setMethodName(parts[7]);
        node.setRemoteIp(parts[8]);

        String spans = parts[9];
        node.setLast(parseDuring(spans));
        node.setTraceName(parts[10]);
        node.setEntrySign(Long.parseLong(parts[11]));
        node.setNodeSign(Long.parseLong(parts[12]));

        return node;
    }

    private Long parseDuring(String spans) {
        String removed = spans.substring(1, spans.length() - 1);
        String[] durings = removed.split(",");
        return Long.valueOf(Integer.parseInt(durings[durings.length - 1]) - Integer.parseInt(durings[0]));
    }
}
