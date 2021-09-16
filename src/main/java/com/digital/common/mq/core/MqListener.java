package com.digital.common.mq.core;


/**
 * 消息监听
 *
 * @author lin
 */
public interface MqListener<T> {

    /**
     * 处理消息
     *
     * @param message message
     */
    void onMessage(T message);
}
