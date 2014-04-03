package com.github.onlysavior.jtrace.store;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-3
 * Time: 下午1:21
 * To change this template use File | Settings | File Templates.
 */
public class LifeCycleSupport {
    private boolean started = false;

    public void start() {
      started = true;
    }

    public void stop() {
      started = false;
    }

    public boolean isRunning() {
        return started;
    }
}
