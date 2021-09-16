package com.github.raylin.mq.core;

import java.util.concurrent.ExecutorService;

/**
 * 生产者线程池
 *
 * @author lilin
 */
public interface MqProducerExecutorServiceProvider {

    /**
     * 生产者线程池配置
     *
     * @return executorService
     */
    ExecutorService buildExecutorService();
}
