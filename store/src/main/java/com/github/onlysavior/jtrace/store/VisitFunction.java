package com.github.onlysavior.jtrace.store;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-2
 * Time: 下午9:22
 * To change this template use File | Settings | File Templates.
 */
public interface VisitFunction<V extends Serializable> {
    public V visit(Serializable originValue);
}
