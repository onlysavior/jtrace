package com.github.onlysavior.jtrace.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 上午10:38
 * To change this template use File | Settings | File Templates.
 */
public class Jtrace {
    static String ROOT_SPAN_ID = "0";
    static String ROOT_PARENT_SPAN_ID = "0";
    static String MAL_ROOT_RPC_ID = "9";

    static final String DELETE_FILE_SUBFIX = ".deleted";
    static public final String RPC_RESULT_SUCCESS = "00";
    static public final String RPC_RESULT_FAILED = "01";

    static final long MAX_SELF_LOG_FILE_SIZE = 200 * 1024 * 1024; // 200MB
    static final long MAX_RPC_LOG_FILE_SIZE = 300 * 1024 * 1024; // 300MB

    static final int LOG_TYPE_TRACE_END = 1;
    static final int LOG_TYPE_RPC_END = 2;
    static final int LOG_TYPE_SERVER_SEND = 3;
    static final int LOG_TYPE_EVENT_FLUSH = -1;

    static public final int TYPE_TRACE = 0;
    static public final int TYPE_RPC_CLIENT = 1;
    static public final int TYPE_PRC_SERVER = 2;

    static final TraceContext TYPE_FLUSH = new TraceContext(LOG_TYPE_EVENT_FLUSH);
    static final int LOG_TYPE_EVENT_ROLLOVER = -2;
    static final TraceContext EVENT_LOG_ROLLOVER = new TraceContext(LOG_TYPE_EVENT_ROLLOVER);
    public static final Charset DEFAULT_CHARACTER;
    static {
        Charset cs;
        try {
            cs = Charset.forName("UTF-8");
        } catch (UnsupportedCharsetException e) {
            cs = Charset.forName("GBK");
        }
        DEFAULT_CHARACTER = cs;
    }

    static final String LOG_FILE_DIR = System.getProperty("user.home")
            + File.separator + "logs" + File.separator + "jtrace";
    private static final String EAGLEEYE_RPC_LOG_FILE = "jtrace.log";
    private static volatile int samplingInterval = 1;
    private static long nextIndexFlushTime = 0L;
    private static final long LOG_CHECK_INTERVAL = TimeUnit.SECONDS.toMillis(15);
    private static AtomicBoolean rpcRecord = new AtomicBoolean(true);


    static private final ThreadLocal<SimpleDateFormat> dateTimeMillisFmt = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            format.setLenient(false);
            return format;
        }
    };
    static private final ThreadLocal<Random> localRandom = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            return new Random();
        }
    };

    private static AsyncLogger rpcAppender = new AsyncLogger(2048);
    private static TraceContextAppender selfAppender = new TraceContextRollingAppender("jtrace-self.log",
            MAX_SELF_LOG_FILE_SIZE, true, false, 8196);

    static final String getJtraceLocation() {
        try {
            URL resource = Jtrace.class.getProtectionDomain().getCodeSource().getLocation();
            if (resource != null) {
                return resource.toString();
            }
        } catch (Throwable t) {
            // ignore
        }
        return "unknown location";
    }

    static private final void startLogCheckThread() {
        final File rpcOnFile = new File(LOG_FILE_DIR + File.separator + "rpc_jtrace.on");
        final File rpcOffFile = new File(LOG_FILE_DIR + File.separator + "rpc_jtrace.off");

        final File rpcFile = new File(LOG_FILE_DIR + File.separator + EAGLEEYE_RPC_LOG_FILE);

        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name != null && name.endsWith(DELETE_FILE_SUBFIX)) {
                    return true;
                }
                return false;
            }
        };

        final Thread deleteLogThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        File logDir = new File(LOG_FILE_DIR);
                        if (logDir.exists() && logDir.isDirectory()) {
                            File[] deleteFiles = logDir.listFiles(filter);
                            if (deleteFiles != null && deleteFiles.length > 0) {
                                for (File f : deleteFiles) {
                                    boolean success = f.delete();
                                    if (success) {
                                        selfLog("[INFO] Deleted log file: " + f.getAbsolutePath());
                                    } else {
                                        selfLog("[ERROR] Fail to delete log file: " + f.getAbsolutePath());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        selfLog("[ERROR] Check and delete log file error", e);
                    }

                    try {
                        if (isRpcOff() && rpcOnFile.exists()) {
                            turnRpcOn();
                        } else if (!isRpcOff() && rpcOffFile.exists()) {
                            turnRpcOff();
                        }
                    } catch (Exception e) {
                        selfLog("[ERROR] Check on/off file error", e);
                    }

                    try {
                        Thread.sleep(LOG_CHECK_INTERVAL);
                    } catch (InterruptedException e) {
                        selfLog("[ERROR] LogDeleteThread Interrupted", e);
                    }

                    try {
                        long now = System.currentTimeMillis();
                        if (now >= nextIndexFlushTime) {
                            /*nextIndexFlushTime = now + INDEX_FLUSH_INTERVAL;
                            for (Entry<String, String> entry : indexes.entrySet()) {
                                index(TYPE_INDEX, entry.getValue(), entry.getKey());
                            }*/
                            //FIXME: index useage
                        }
                    } catch (Exception e) {
                        selfLog("[ERROR] Output index table error", e);
                    }

                    if (!rpcFile.exists()) {
                        rpcAppender.append(EVENT_LOG_ROLLOVER);
                    }

                    flush();
                }
            }
        });
        deleteLogThread.setDaemon(true);
        deleteLogThread.setName("Jtrace-LogCheck-Thread");
        deleteLogThread.start();
    }

    static private final void startLogger() {
        TraceContextAppender rpcLogger = new TraceContextRollingAppender(
                EAGLEEYE_RPC_LOG_FILE, MAX_RPC_LOG_FILE_SIZE, true, true);
        rpcAppender.start(rpcLogger, new DefaultContextEncoder());
    }

    private static void flush() {
        rpcAppender.flush();
    }

    public static void selfLog(String log) {
        try {
            String line = "[" + dateTimeMillisFmt.get().format(new Date()) + "] " + log + StringUtils.NEW_LINE;
            selfAppender.append(line);
        } catch (Throwable t) {
        }
    }

    public static void selfLog(String log, Throwable e) {
        try {
            StringWriter sw = new StringWriter(4096);
            PrintWriter pw = new PrintWriter(sw, false);
            pw.append('[').append(dateTimeMillisFmt.get().format(new Date()))
                    .append("] ").append(log).append(StringUtils.NEW_LINE);
            e.printStackTrace(pw);
            pw.println();
            pw.flush();
            selfAppender.append(sw.toString());
        } catch (Throwable t) {
        }
    }

    public static int getSamplingInterval() {
        return samplingInterval;
    }

    public static void setSamplingInterval(int interval) {
        samplingInterval = interval;
    }

    public static boolean isRpcOff() {
        return !rpcRecord.get();
    }

    public static void turnRpcOn() {
        selfLog("[INFO] turnRpcOn");
        rpcRecord.set(true);
    }

    public static void turnRpcOff() {
        selfLog("[INFO] turnRpcOff");
        rpcRecord.set(false);
    }

    public static boolean checkSamping(String traceId) {
        if (samplingInterval <= 1 || samplingInterval >= 1000) {
            return true;
        }

        Random random = localRandom.get();
        int sample = random.nextInt(1000);
        if (sample <= samplingInterval) {
            return true;
        }
        return false;
    }

    static {
        try {
            startLogger();
        } catch (Throwable e) {
            selfLog("[ERROR] fail to start EagleEye logger", e);
        }
        try {
            startLogCheckThread();
        } catch (Throwable e) {
            selfLog("[ERROR] fail to start EagleEye log check thread", e);
        }
        selfLog("[INFO] EagleEye started (" + getJtraceLocation() + ")");
    }






    //API
    public static void startTrace(String traceId, String traceName) {
        if (!StringUtils.isNotBlank(traceName)) {
            return;
        }
        TraceContext context = TraceContext.get();
        if (context != null && context.traceId != null) {
            if (!context.traceId.equals(traceId) || !context.traceName.equals(traceName)) {
                selfLog("[WARN] duplicated startTrace detected, overrided " + context.traceId +
                        " (" + context.traceName + ") to " + traceId + " (" + traceName + ")");
                endTrace(null, TYPE_TRACE);
            } else {
                return;
            }
        }
        if (!StringUtils.isNotBlank(traceId)) {
            traceId = TraceIdGen.generate();
        }

        try {
            context = new TraceContext(traceId);
            TraceContext.set(context);
            context.startTrace(traceName);
        } catch (Throwable e) {
            selfLog("[ERROR] startTrace", e);
        }
    }

    public static void endTrace(String resultCode, int type) {
        try {
            TraceContext currentContext = TraceContext.get();
            if (currentContext == null) {
                return;
            }
            TraceContext root = currentContext;
            while (root.parentTraceContext != null) {
                root = root.parentTraceContext;
            }

            root.endTrace(resultCode, type);
            commitTraceContext(root);
        } finally {
            clearTraceContext();
        }
    }

    public static Object currentTraceContext() {
        if (TraceContext.get() != null) {
            return TraceContext.get();  //Serilze
        }
        return null;
    }

    public static TraceContext pop() {
        TraceContext current = TraceContext.get();
        if (current == null) {
            return null;
        }
        TraceContext.set(current.parentTraceContext);
        return current;
    }

    public static void startRpc(String severName, String method) {
        TraceContext current = TraceContext.get();
        TraceContext child;
        if (current == null) {
            child = new TraceContext(TraceIdGen.generate(), ROOT_SPAN_ID, MAL_ROOT_RPC_ID);
        } else {
            child = current.createChildContext();
        }
        TraceContext.set(child);
        child.startRpc(severName, method);
    }

    public static void rpcClientSend(String remoteIp) {
        rpcClientSend();
        setRemoteIp(remoteIp);
    }

    public static void rpcClientSend() {
        TraceContext current = TraceContext.get();
        if (current == null) {
            return;
        }
        current.rpcClientSend();
    }

    public static void rpcClientRecv() {
        rpcClientRecv(RPC_RESULT_SUCCESS, TYPE_RPC_CLIENT);
    }

    public static void rpcClientRecv(String resultCode, int type) {
        TraceContext current = TraceContext.get();
        if (current == null) {
            return;
        }
        current.endTrace(resultCode, type);
        commitTraceContext(current);
        pop();
    }

    public static void rpcFail(String resultCode) {
        rpcClientRecv(resultCode, TYPE_RPC_CLIENT);
    }

    public static void rpcServerRecv(String clientIp, String serverName, String method) {
        rpcServerRecv(serverName, method);
        setRemoteIp(clientIp);
    }

    public static void rpcServerRecv(String serverName, String method) {
        TraceContext current = TraceContext.get();
        if (current == null) {
            return;
        }
        current.rpcServerRecv(serverName, method, current.nodeSign);
    }

    public static void rpcServerSend() {
        rpcServerSend(TYPE_PRC_SERVER);
    }

    public static void rpcServerSend(int type) {
        TraceContext current = TraceContext.get();
        if (current == null) {
            return;
        }
        current.rpcServerSend(type);
        commitTraceContext(current);
    }

    public static void clearTraceContext() {
        TraceContext.set(null);
    }

    private static void commitTraceContext(TraceContext context) {
        if (!isRpcOff() && checkSamping(context.traceId)) {
            rpcAppender.append(context);
        }
    }

    private static void setRemoteIp(String ip) {
        TraceContext current = TraceContext.get();
        if (current == null) {
            return;
        }
        current.setRemoteIp(ip);
    }
}
