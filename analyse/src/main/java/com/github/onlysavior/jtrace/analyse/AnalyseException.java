package com.github.onlysavior.jtrace.analyse;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-3
 * Time: 下午10:53
 * To change this template use File | Settings | File Templates.
 */
public class AnalyseException extends RuntimeException {
    public AnalyseException() {
    }

    public AnalyseException(String message) {
        super(message);
    }

    public AnalyseException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnalyseException(Throwable cause) {
        super(cause);
    }
}

