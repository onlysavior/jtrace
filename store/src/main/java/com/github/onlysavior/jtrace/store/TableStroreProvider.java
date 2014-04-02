package com.github.onlysavior.jtrace.store;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-2
 * Time: 下午9:33
 * To change this template use File | Settings | File Templates.
 */
public interface TableStroreProvider extends AccessableStroreProvider {
    public void updateOrStore(String rowKey, int rt);
    public void updateOrStore(ByteBuffer rowKey, int rt);
}
