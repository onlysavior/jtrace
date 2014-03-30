package com.github.onlysavior.jtrace.core;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 下午3:14
 * To change this template use File | Settings | File Templates.
 */
public interface TraceContextEncoder {
    void encode(TraceContext context, TraceContextAppender appender) throws IOException;
}
