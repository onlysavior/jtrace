package com.github.onlysavior.jtrace.store;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-2
 * Time: 下午9:33
 * To change this template use File | Settings | File Templates.
 */
public interface TableStroreProvider extends AccessableStroreProvider {
    public void storeInPath(String rowKey, long count);
    public void storeOutPath(String rowKey, long count);
}
