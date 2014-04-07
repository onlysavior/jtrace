package com.github.onlysavior.jtrace.store;

/**
 * Created by Administrator on 14-4-7.
 */
public class StoreProviders {

    public static HdfsStoreProvider hdfs() {
        return new HdfsStoreProvider();
    }

    public static FileStoreProvider file(String base) {
        return new FileStoreProvider(base);
    }

    public static MySQLStoreProvider sql(String connectionInfo) {
        return new MySQLStoreProvider(connectionInfo);
    }

    public static RRDStoreProvider rrd(String base, String appName) {
        return new RRDStoreProvider(base, appName);
    }
}
