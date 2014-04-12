package com.github.onlysavior.jtrace.analyse;

import com.github.onlysavior.jtrace.analyse.offline.OffLineAnalyse;
import com.github.onlysavior.jtrace.analyse.runtime.RuntimeAnalyse;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-12
 * Time: 下午1:16
 * To change this template use File | Settings | File Templates.
 */
public class Analyse {
    public static void main(String[] args) {
        final OffLineAnalyse offLineAnalyse = new OffLineAnalyse();
        final RuntimeAnalyse runtimeAnalyse = new RuntimeAnalyse();
        offLineAnalyse.start();
        runtimeAnalyse.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                offLineAnalyse.stop();
                runtimeAnalyse.stop();
            }
        });
    }
}
