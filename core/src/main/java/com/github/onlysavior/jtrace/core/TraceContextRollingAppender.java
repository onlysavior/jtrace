package com.github.onlysavior.jtrace.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 下午4:06
 * To change this template use File | Settings | File Templates.
 */
public class TraceContextRollingAppender implements TraceContextAppender {
    private static final long LOG_FLUSH_INTERVAL = TimeUnit.SECONDS.toMillis(1);
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024; // 8KB
    private int maxBackupIndex = 1;

    private final long maxFileSize;
    private final int bufferSize;
    private final String fileName;
    private final AtomicBoolean isRolling = new AtomicBoolean(false);
    private File logFile = null;
    private BufferedOutputStream bos = null;
    private long nextFlushTime = 0L;
    private long lastRollOverTime = 0L;

    private final boolean selfLogEnabled;

    public TraceContextRollingAppender(String file, long maxFileSize, boolean append,
                                       boolean selfLogEnabled) {
        this(file, maxFileSize, append, selfLogEnabled, DEFAULT_BUFFER_SIZE);
    }

    public TraceContextRollingAppender(String file, long maxFileSize, boolean append,
                                       boolean selfLogEnabled, int bufferSize) {
        this.fileName = file;
        this.maxFileSize = maxFileSize;
        this.bufferSize = bufferSize;
        this.selfLogEnabled = selfLogEnabled;
        setFile(append);
    }

    private void setFile(boolean append) {
        try {
            logFile = new File(fileName);
            if (!logFile.exists()) {
                File parentDic = logFile.getParentFile();
                if (parentDic != null && !parentDic.exists() && !parentDic.mkdirs()) {
                    //failed to parent dirs when parent dir not exist
                    doSelfLog("[ERROR] Fail to mkdirs: " + parentDic.getAbsolutePath());
                    return;
                }

                if (!logFile.createNewFile()) {
                    doSelfLog("[ERROR] Fail to create file to write: " + logFile.getAbsolutePath());
                    return;
                }
            }

            if (!logFile.isFile() || !logFile.canWrite()) {
                doSelfLog("[ERROR] Invalid file, exists=" + logFile.exists() +
                        ", isFile=" + logFile.isFile() +
                        ", canWrite=" + logFile.canWrite() +
                        ", path=" + logFile.getAbsolutePath());
                return;
            }
            FileOutputStream ostream = new FileOutputStream(logFile, append);
            bos = new BufferedOutputStream(ostream, bufferSize);
            lastRollOverTime = System.currentTimeMillis();
        } catch (IOException e) {
            //noop
        }
    }

    @Override
    public void append(String log) throws IOException {
        if (bos != null) {
            waitUntilRollFinish();
            bos.write(log.getBytes(Jtrace.DEFAULT_CHARACTER));
            if (logFile.length() > maxFileSize && isRolling.compareAndSet(false, true)) {
                try {
                    rollOver();
                    nextFlushTime = System.currentTimeMillis() + LOG_FLUSH_INTERVAL;
                } finally {
                    isRolling.set(false);
                }
            } else {
                long now;
                if ((now = System.currentTimeMillis()) >= nextFlushTime) {
                    bos.flush();
                    nextFlushTime = now + LOG_FLUSH_INTERVAL;
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (bos != null) {
            bos.flush();
        }
    }

    @Override
    public void rollOver() throws IOException {
        File target;
        File file;

        if (maxBackupIndex > 0) {
            file = new File(fileName + '.' + maxBackupIndex);
            if (file.exists()) {
                target = new File(fileName + '.' + maxBackupIndex + Jtrace.DELETE_FILE_SUBFIX);
                if (!file.renameTo(target) && !file.delete()) {
                    doSelfLog("[ERROR] Fail to delete or rename file: " + file.getAbsolutePath() +
                            " to " + target.getAbsolutePath());
                }
            }

            for (int i = maxBackupIndex - 1; i >= 1; i--) {
                file = new File(fileName + '.' + i);
                if (file.exists()) {
                    target = new File(fileName + '.' + (i + 1));
                    if (!file.renameTo(target) && !file.delete()) {
                        doSelfLog("[ERROR] Fail to delete or rename file: " + file.getAbsolutePath() +
                                " to " + target.getAbsolutePath());
                    }
                }
            }

            target = new File(fileName + "." + 1);

            try {
                bos.close();
            } catch (IOException e) {
                doSelfLog("[WARN] Fail to close OutputStream: " + e.getMessage());
            }

            file = new File(fileName);
            if (file.renameTo(target)) {
                doSelfLog("[INFO] File rolled over, " +
                        TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastRollOverTime) +
                        " minutes since last roll");
            } else {
                doSelfLog("[WARN] Fail to rename file: " + file.getAbsolutePath() +
                        " to " + target.getAbsolutePath());
            }
            setFile(false);
        }
    }

    private void doSelfLog(String log) {
        if (selfLogEnabled) {
            Jtrace.selfLog(log);
        } else {
            System.out.println("[EagleEye]" + log);
        }
    }

    private void waitUntilRollFinish() {
        while (isRolling.get()) {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                //noop
            }
        }
    }
}
