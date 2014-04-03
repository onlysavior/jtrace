package com.github.onlysavior.jtrace.store;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-4-3
 * Time: 下午1:30
 * To change this template use File | Settings | File Templates.
 */
public class RRDStoreProvider extends LifeCycleSupport {
    private static final String RRD_DATABASE_NAME = "jtrace.rrd";

    private String basePath;
    private String applicationName;
    private RrdDef rrdDef;
    private RrdDb rrdDb;

    public RRDStoreProvider(String basePath, String applicationName) {
        this.basePath = basePath;
        this.applicationName = applicationName;
    }

    @Override
    public void start() {
        super.start();
        rrdDef = new RrdDef(basePath + File.pathSeparator + RRD_DATABASE_NAME);
        rrdDef.setStartTime(new Date());
        rrdDef.addDatasource(applicationName + "_qps", DsType.COUNTER, 600, Double.NaN, Double.NaN);
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 24);
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 6, 10);

        try {
            rrdDb = new RrdDb(rrdDef);
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        try {
            rrdDb.close();
        } catch (IOException e) {
            throw new StoreException(e); //ignore?
        }
    }

    public void sample(long time, Double value) {
        try {
            Sample sample = rrdDb.createSample();
            sample.setTime(time);
            sample.setValue(applicationName + "_qps", value);
            sample.update();
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    //FIXME add method to support drawing
}
