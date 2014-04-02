package com.github.onlysavior.jtrace.store;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-2
 * Time: 下午9:27
 * To change this template use File | Settings | File Templates.
 */
public interface SequenceStroreProvider extends AccessableStroreProvider {
    public void store(String traceId, String info);
    public void store(String traceId, ByteBuffer info);
}
