package com.github.onlysavior.jtrace.core;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 下午3:12
 * To change this template use File | Settings | File Templates.
 */
public interface TraceContextAppender {
    public abstract void append(String log) throws IOException;
    public abstract void flush() throws IOException;
    public abstract void rollOver() throws IOException;
}
