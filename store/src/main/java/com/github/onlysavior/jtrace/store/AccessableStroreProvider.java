package com.github.onlysavior.jtrace.store;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-2
 * Time: 下午9:11
 * To change this template use File | Settings | File Templates.
 */
public interface AccessableStroreProvider {
    public List<Serializable> byId(String traceId);
    public List<Serializable> range(String min, String max);
    public List<Serializable> visit(VisitFunction function);
}
