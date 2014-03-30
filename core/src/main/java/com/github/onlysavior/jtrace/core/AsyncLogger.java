package com.github.onlysavior.jtrace.core;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-3-30
 * Time: 下午2:57
 * To change this template use File | Settings | File Templates.
 */
public class AsyncLogger {
    private Disruptor<TraceContext> disruptor = null;
    private RingBuffer<TraceContext> ringBuffer = null;
    private TraceContextAppender appender = null;
    private TraceContextEncoder encoder = null;

    private volatile boolean running = false;

    public AsyncLogger(int queueSize) {
        queueSize = 1 << (32 - Integer.numberOfLeadingZeros(queueSize - 1));
        EventFactory<TraceContext> traceContextEventFactory = new TraceContextEventFactory();
        Executor executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });

        TraceLogHanlder traceLogHanlder = new TraceLogHanlder();
        disruptor = new Disruptor<TraceContext>(traceContextEventFactory,
                queueSize, executor, ProducerType.SINGLE, new YieldingWaitStrategy());
        disruptor.handleEventsWith(traceLogHanlder);
        ringBuffer = disruptor.getRingBuffer();
    }

    public void start(TraceContextAppender appender, TraceContextEncoder encoder) {
        this.appender = appender;
        this.encoder = encoder;

        disruptor.start();
        running = true;
    }

    public void stop() throws TimeoutException {
        running = false;
        disruptor.shutdown(10, TimeUnit.SECONDS);
    }

    public void append(TraceContext context) {
        if (!checkRunning()) {
            return;
        }

        long sequence = ringBuffer.next();
        TraceContext preAlocate = ringBuffer.get(sequence);
        preAlocate.copy(context);
        ringBuffer.publish(sequence);
    }

    public void flush() {
        append(Jtrace.TYPE_FLUSH);
        long end = System.currentTimeMillis() + 500;
        while (ringBuffer.remainingCapacity() < ringBuffer.getBufferSize()
                && System.currentTimeMillis() <= end) {
            if (checkRunning()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                //FIXME: how to make ringbuffer wakeup
            }
        }
    }

    private boolean checkRunning() {
        return running;
    }

    class TraceContextEventFactory implements EventFactory<TraceContext> {

        @Override
        public TraceContext newInstance() {
            return new TraceContext();
        }
    }

    class TraceLogHanlder implements EventHandler<TraceContext> {

        @Override
        public void onEvent(TraceContext event, long sequence, boolean endOfBatch) throws Exception {
            final TraceContextEncoder traceContextEncoder = AsyncLogger.this.encoder;
            final TraceContextAppender traceContextAppender = AsyncLogger.this.appender;

            traceContextEncoder.encode(event, traceContextAppender);
        }
    }
}
