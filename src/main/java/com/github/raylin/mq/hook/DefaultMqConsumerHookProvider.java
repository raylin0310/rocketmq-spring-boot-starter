package com.github.raylin.mq.hook;

import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author lin
 */
public class DefaultMqConsumerHookProvider implements MqConsumerHookProvider {

    private List<MqConsumerHook> mqConsumerHooks;

    public DefaultMqConsumerHookProvider(List<MqConsumerHook> mqConsumerHooks) {
        this.mqConsumerHooks = mqConsumerHooks;
    }

    @Override
    public MqConsumerHookChain getMqConsumerHookChain(MessageExt messageExt) {
        return new MqConsumerHookChain(mqConsumerHooks);
    }
}
