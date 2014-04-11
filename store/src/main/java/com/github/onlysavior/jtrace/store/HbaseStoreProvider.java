package com.github.onlysavior.jtrace.store;

import com.github.onlysavior.jtrace.core.StringUtils;
import com.github.onlysavior.jtrace.core.TraceSign;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-4-7.
 */
public class HbaseStoreProvider extends LifeCycleSupport implements TableStroreProvider,FastLocate {
    private static final String TABLE_NAME = "jtrace";
    private static final String FAST_LOCATE_NAME = "fastlocate";

    private Configuration configuration;

    @Override
    public void start() {
        super.start();
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum",
                "master");
        configuration.set("hbase.zookeeper.property.clientPort", "2181");

        try {
            HBaseAdmin hAdmin = new HBaseAdmin(configuration);

            if (!hAdmin.tableExists(TABLE_NAME)) {
                createTable(hAdmin, TABLE_NAME);
            }

            if (!hAdmin.tableExists(FAST_LOCATE_NAME)) {
                createTable(hAdmin, FAST_LOCATE_NAME);
            }
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }

    public void updateRT(String rowKey, int rt) {
        try {
            HTable table = new HTable(configuration, TABLE_NAME);
            Put put = new Put(Bytes.toBytes("+"+rowKey));
            put.add(Bytes.toBytes("data"), Bytes.toBytes("rt"), Bytes.toBytes(rt));
            table.put(put);
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }


    @Override
    public List<Serializable> byId(String path) {
        try {
            HTable table = new HTable(configuration, TABLE_NAME);
            Get get = new Get(Bytes.toBytes(path));
            get.addColumn(Bytes.toBytes("data"), Bytes.toBytes("rt"));
            get.addColumn(Bytes.toBytes("data"), Bytes.toBytes("qps"));
            Result result = table.get(get);

            List<Serializable> rtn = new ArrayList<Serializable>();
            Row row = new Row();
            for(KeyValue keyValue : result.raw()) {
                String colum = Bytes.toString(keyValue.getQualifier());
                byte[] value = keyValue.getValue();
                if("path".equals(colum)) {
                    row.setPath(Bytes.toString(value));
                }
                if("qps".equals(colum)){
                    row.setQps(Bytes.toLong(value));
                }
                if ("rt".equals(colum)) {
                    row.setRt(Bytes.toInt(value));
                }
            }
            rtn.add(row);
            return rtn;

        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public List<Serializable> range(String min, String max) {
        try {
            //FIXME custom filter to accept two pattern
            HTable table = new HTable(configuration, TABLE_NAME);
            Scan scan = new Scan();
            Filter less = new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL,
                    new BinaryPrefixComparator(Bytes.toBytes(min)));
            Filter greate = new RowFilter(CompareFilter.CompareOp.GREATER,
                    new BinaryPrefixComparator(Bytes.toBytes(max)));
            FilterList filter = new FilterList(less,greate);
            scan.setFilter(filter);

            ResultScanner scanner = table.getScanner(scan);
            List<Serializable> rtn = new ArrayList<Serializable>();
            for(Result result : scanner) {
                Row row = new Row();
                for(KeyValue keyValue : result.raw()) {
                    String colum = Bytes.toString(keyValue.getQualifier());
                    byte[] value = keyValue.getValue();
                    if("path".equals(colum)) {
                        row.setPath(Bytes.toString(value));
                    }
                    if("qps".equals(colum)){
                        row.setQps(Bytes.toLong(value));
                    }
                    if ("rt".equals(colum)) {
                        row.setRt(Bytes.toInt(value));
                    }
                }
                rtn.add(row);
            }
            return rtn;
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public List<Serializable> visit(VisitFunction function) {
        //FIXME Coprocessors
        return null;
    }

    @Override
    public String locate(long entrySign, long nodeSign) {
        try {
            HTable table = new HTable(configuration, FAST_LOCATE_NAME);
            Get get = new Get(Bytes.toBytes(""+entrySign+":"+nodeSign));
            get.addColumn(Bytes.toBytes("data"),Bytes.toBytes("path"));

            Result result = table.get(get);
            for(KeyValue keyValue : result.raw()) {
                String colum = Bytes.toString(keyValue.getQualifier());
                byte[] value = keyValue.getValue();
                if("path".equals(colum)) {
                    return Bytes.toString(value);
                }
            }
        } catch (IOException e) {
            throw new StoreException(e);
        }
        return null;
    }

    private void putJtrace(byte[] rowKey, long rt) throws IOException {
        HTable table = new HTable(configuration, TABLE_NAME);
        Put put = new Put(rowKey);
        put.add(Bytes.toBytes("data"), Bytes.toBytes("totalcount1hour"), Bytes.toBytes(rt));


        Increment inc = new Increment(rowKey);
        inc.setTimeRange(0L, 1000L);
        inc.addColumn(Bytes.toBytes("data"), Bytes.toBytes("qps"), 1);

        table.put(put);
        table.increment(inc);
    }

    private void putFastLocate(String rowKey, boolean in) {
        String entry = TraceSign.getEntry(rowKey);
        if (StringUtils.isNotBlank(entry)) {
            long entrySign = entry.hashCode();
            long nodeSign = TraceSign.sign(rowKey);

            try {
                HTable table = new HTable(configuration, FAST_LOCATE_NAME);
                Put put = new Put(Bytes.toBytes(""+entrySign+":"+nodeSign));
                if (in) {
                    put.add(Bytes.toBytes("data"), Bytes.toBytes("path"), Bytes.toBytes("+"+rowKey));
                } else {
                    put.add(Bytes.toBytes("data"), Bytes.toBytes("path"), Bytes.toBytes("-"+rowKey));
                }

                table.put(put);
            } catch (IOException e) {
                throw new StoreException(e);
            }

        }
    }

    private void createTable(HBaseAdmin admin, String tableName) throws IOException {
        HTableDescriptor tableDesc = new HTableDescriptor(tableName);
        tableDesc.addFamily(new HColumnDescriptor("data"));
        admin.createTable(tableDesc);
    }

    @Override
    public void storeInPath(String rowKey, long count) {
        try {
            putJtrace(Bytes.toBytes("+"+rowKey), count);
            putFastLocate(rowKey, true);
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public void storeOutPath(String rowKey, long count) {
        try {
            putJtrace(Bytes.toBytes("-"+rowKey), count);
            putFastLocate(rowKey, false);
        }catch (IOException e) {
            throw new StoreException(e);
        }
    }

    public static class Row implements Serializable {
        private String path;
        private int rt;
        private long qps;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getRt() {
            return rt;
        }

        public void setRt(int rt) {
            this.rt = rt;
        }

        public long getQps() {
            return qps;
        }

        public void setQps(long qps) {
            this.qps = qps;
        }
    }
}
