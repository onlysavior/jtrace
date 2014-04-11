package com.github.onlysavior.jtrace.collector;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-11
 * Time: 下午6:43
 * To change this template use File | Settings | File Templates.
 */
public class CollectorException extends RuntimeException {
    public CollectorException() {
    }

    public CollectorException(String message) {
        super(message);
    }

    public CollectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectorException(Throwable cause) {
        super(cause);
    }
}
