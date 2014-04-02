package com.github.onlysavior.jtrace.store;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-2
 * Time: 下午9:28
 * To change this template use File | Settings | File Templates.
 */
public class StoreException extends RuntimeException {
    public StoreException() {
    }

    public StoreException(String message) {
        super(message);
    }

    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoreException(Throwable cause) {
        super(cause);
    }
}
