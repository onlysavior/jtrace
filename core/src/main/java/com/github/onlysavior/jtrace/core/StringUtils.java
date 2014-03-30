package com.github.onlysavior.jtrace.core;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 下午12:30
 * To change this template use File | Settings | File Templates.
 */
public abstract class StringUtils {
    public static final String EMPTY_STRING = "";
    public static final String NEW_LINE = "\r\n";

    public static boolean isNotBlank(String str) {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        return true;
    }
}
