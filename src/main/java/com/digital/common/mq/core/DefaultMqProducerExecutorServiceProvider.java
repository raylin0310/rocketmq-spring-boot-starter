package com.digital.common.mq.core;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lin
 */
public class DefaultMqProducerExecutorServiceProvider implements MqProducerExecutorServiceProvider {

    @Override
    public ExecutorService buildExecutorService() {
        AtomicLong counter = new AtomicLong();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(availableProcessors, availableProcessors, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("default-mq-producer-thread-" + counter.getAndIncrement());
                return thread;
            }
        });
    }
}
