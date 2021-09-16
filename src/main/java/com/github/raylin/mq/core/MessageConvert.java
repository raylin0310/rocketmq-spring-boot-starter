package com.github.raylin.mq.core;

import org.apache.rocketmq.common.message.Message;

/**
 * 消息序列化转换
 *
 * @author lilin
 */
public interface MessageConvert {

    /**
     * 反序列化
     *
     * @param message     message
     * @param targetClass targetClass
     * @return object
     * @throws Exception convert exception
     */
    Object fromMessage(Message message, Class<?> targetClass) throws Exception;

    /**
     * 消息序列化
     *
     * @param payload msg
     * @return byte[]
     */
    byte[] toMessage(Object payload);
}
