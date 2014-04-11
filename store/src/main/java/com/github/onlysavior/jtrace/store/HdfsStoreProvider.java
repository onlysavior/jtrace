package com.github.onlysavior.jtrace.store;

import com.github.onlysavior.jtrace.core.Jtrace;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.io.IOUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 14-4-7.
 */
public class HdfsStoreProvider extends LifeCycleSupport implements SequenceStroreProvider {
    public static final String URI_SPACE = "hdfs://localhost:9000/jtrace/";
    public static final String FILE_NAME = "tracedata";
    public static final String OUTPUT1 = "mr1output";
    public static final String OUTPUT2 = "mr2output";

    private Configuration configuration;
    private FileSystem fileSystem;
    private DistributedFileSystem distributedFileSystem;

    @Override
    public void start() {
        super.start();
        try {
            configuration = new Configuration();
            configuration.addResource(URI_SPACE);
            fileSystem = FileSystem.get(URI.create(URI_SPACE), configuration);
            distributedFileSystem = (DistributedFileSystem)fileSystem;
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        IOUtils.closeStream(fileSystem);
        fileSystem = null;
        distributedFileSystem = null;
    }

    @Override
    public void store(String traceId, String info) {
       store(traceId, ByteBuffer.wrap(info.getBytes(Jtrace.DEFAULT_CHARACTER)));
    }

    @Override
    public void store(String traceId, ByteBuffer info) {
        String dateOneHour = extractDate(traceId);
        Path path = new Path(URI_SPACE,dateOneHour);
        try {
            if(!fileSystem.exists(path)) {
                fileSystem.mkdirs(path);
            }
            Path dataFilePath = new Path(path, FILE_NAME);
            FSDataOutputStream fos = distributedFileSystem.create(dataFilePath);
            fos.write(info.array());
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public List<Serializable> byId(String traceId) {
       throw new UnsupportedOperationException("no support by HDFS");
    }

    @Override
    public List<Serializable> range(String min, String max) {
        throw new UnsupportedOperationException("no support by HDFS");
    }

    @Override
    public List<Serializable> visit(VisitFunction function) {
        throw new UnsupportedOperationException("no support by HDFS");
    }

    private String extractDate(String traceId) {
        assert traceId != null;
        assert traceId.length() == 25;
        Long date =  Long.parseLong(traceId.substring(8, 21));
        Date d = new Date(date);
        return "input/" + DateUtils.round(d, Calendar.HOUR).getTime();
    }
}
