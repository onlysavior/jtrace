package com.github.onlysavior.jtrace.core;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 下午5:28
 * To change this template use File | Settings | File Templates.
 */
public class DefaultContextEncoder implements TraceContextEncoder {
    private static final int DEFAULT_STRINGBUILDER_SIZE = 256;
    private ThreadLocal<StringBuilder> local = new ThreadLocal<StringBuilder>(){
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(DEFAULT_STRINGBUILDER_SIZE);
        }
    };
    @Override
    public void encode(TraceContext ctx, TraceContextAppender eea) throws IOException {
        StringBuilder buffer = local.get();
        buffer.delete(0, buffer.length());

        switch (ctx.logType) {
            case Jtrace.LOG_TYPE_TRACE_END:
                buffer.append(ctx.traceId).append('|')
                        .append(ctx.startTime).append('|')
                        .append(ctx.logType).append('|')
                        .append(ctx.rpcType).append('|')
                        .append(ctx.spanId).append('|')
                        .append(ctx.endTime - ctx.startTime).append('|')
                        .append(ctx.traceName).append("|")
                        .append(ctx.entrySign).append("|")
                        .append(ctx.nodeSign);
                if (StringUtils.isNotBlank(ctx.serverName)) {
                    buffer.append(ctx.serverName).append('|');
                    if (StringUtils.isNotBlank(ctx.remoteIp)) {
                        buffer.append(ctx.remoteIp).append('|');
                    }
                }
                break;
            case Jtrace.LOG_TYPE_RPC_END:
                buffer.append(ctx.traceId).append('|')
                        .append(ctx.startTime).append('|')
                        .append(ctx.logType).append('|')
                        .append(ctx.rpcType).append('|')
                        .append(ctx.parentSpanId).append('|')
                        .append(ctx.spanId).append('|')
                        .append(ctx.serverName).append('|')
                        .append(ctx.methodName).append('|')
                        .append(ctx.remoteIp).append('|')
                        .append('[').append(ctx.span0).append(", ").append(ctx.spna1).append(']').append('|')
                        .append(ctx.traceName).append("|")
                        .append(ctx.entrySign).append("|")
                        .append(ctx.nodeSign);
                break;
            case Jtrace.LOG_TYPE_SERVER_SEND:
                buffer.append(ctx.traceId).append('|')
                        .append(ctx.startTime).append('|')
                        .append(ctx.logType).append('|')
                        .append(ctx.rpcType).append('|')
                        .append(ctx.endTime - ctx.startTime)
                        .append(ctx.parentSpanId).append('|')
                        .append(ctx.spanId).append('|');
                buffer.append(ctx.remoteIp).append('|')
                        .append(ctx.entrySign).append('|')
                        .append(ctx.nodeSign);
                if (StringUtils.isNotBlank(ctx.serverName) && StringUtils.isNotBlank(ctx.methodName)) {
                    buffer.append('|');
                    buffer.append(ctx.serverName).append('|');
                    buffer.append(ctx.methodName).append('|');
                }
                break;
            /*case Jtrace.LOG_TYPE_RPC_LOG:
                buffer.append(ctx.traceId).append('|')
                        .append(ctx.logTime).append('|')
                        .append(ctx.rpcType);
                if (StringUtils.isNotBlank(ctx.rpcId)) {
                    buffer.append('|').append(ctx.rpcId);
                }
                break;
            case Jtrace.LOG_TYPE_INDEX:
                buffer.append(ctx.traceId).append('|')
                        .append(ctx.logTime).append('|')
                        .append(ctx.rpcType).append('|')
                        .append(ctx.traceName).append('|') // index
                        .append(ctx.callBackMsg).append(StringUtils.NEWLINE);
                eea.append(buffer.toString());
                return;*/
            case Jtrace.LOG_TYPE_EVENT_FLUSH:
                eea.flush();
                return;
/*            case Jtrace.LOG_TYPE_EVENT_ROLLOVER:
                eea.rollOver();
                return;*/
            default:
                // ignore
                return;
        }

        final int samplingInterval = Jtrace.getSamplingInterval();
        if (samplingInterval >= 2 && samplingInterval <= 9999) {
            buffer.append("|#").append(samplingInterval);
        }


        buffer.append(StringUtils.NEW_LINE);

        eea.append(buffer.toString());
    }
}
