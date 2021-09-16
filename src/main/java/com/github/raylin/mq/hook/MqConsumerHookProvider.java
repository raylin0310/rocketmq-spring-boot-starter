package com.github.raylin.mq.hook;

import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author lin
 */
public interface MqConsumerHookProvider {

    /**
     * 创建hook chain
     *
     * @param messageExt msg
     * @return hook chain
     */
    MqConsumerHookChain getMqConsumerHookChain(MessageExt messageExt);
}
