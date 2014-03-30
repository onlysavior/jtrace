package com.github.onlysavior.jtrace.core;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 上午10:38
 * To change this template use File | Settings | File Templates.
 */
public class Jtrace {
    static String ROOT_SPAN_ID = "0";
    static String ROOT_PARENT_SPAN_ID = "0";
    static final String DELETE_FILE_SUBFIX = ".deleted";

    static final int LOG_TYPE_TRACE_END = 1;
    static final int LOG_TYPE_RPC_END = 2;
    static final int LOG_TYPE_SERVER_SEND = 3;
    static final int LOG_TYPE_EVENT_FLUSH = -1;

    static final TraceContext TYPE_FLUSH = new TraceContext(LOG_TYPE_EVENT_FLUSH);
    static final Charset DEFAULT_CHARACTER;
    static {
        Charset cs;
        try {
            cs = Charset.forName("UTF-8");
        } catch (UnsupportedCharsetException e) {
            cs = Charset.forName("GBK");
        }
        DEFAULT_CHARACTER = cs;
    }

    private static int samplingInterval;

    public static void selfLog(String log) {
        //TODO selfLog
    }

    public static int getSamplingInterval() {
        return samplingInterval;
    }
}
