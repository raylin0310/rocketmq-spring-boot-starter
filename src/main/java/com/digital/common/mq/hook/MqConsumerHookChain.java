package com.digital.common.mq.hook;

import com.digital.common.mq.core.LionMqConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author lin
 */
public class MqConsumerHookChain {

    private final static Logger log = LoggerFactory.getLogger(LionMqConsumer.class);

    private final List<MqConsumerHook> mqConsumerHooks;

    private int hookIndex = -1;

    public MqConsumerHookChain(List<MqConsumerHook> mqConsumerHooks) {
        this.mqConsumerHooks = mqConsumerHooks;
    }

    public boolean applyPreHandle(MessageExt messageExt, HookContext context) {
        if (mqConsumerHooks == null || mqConsumerHooks.size() == 0) {
            return true;
        }
        for (int i = 0; i < mqConsumerHooks.size(); i++) {
            MqConsumerHook consumerHook = mqConsumerHooks.get(i);
            if (!consumerHook.preHandle(messageExt, context)) {
                triggerAfterCompletion(messageExt, context, null);
                return false;
            }
            this.hookIndex = i;
        }
        return true;
    }

    public void applyPostHandle(MessageExt messageExt, HookContext context) {
        if (mqConsumerHooks == null || mqConsumerHooks.size() == 0) {
            return;
        }
        for (int i = mqConsumerHooks.size() - 1; i >= 0; i--) {
            MqConsumerHook consumerHook = mqConsumerHooks.get(i);
            consumerHook.postHandle(messageExt, context);
        }
    }

    public void triggerAfterCompletion(MessageExt messageExt, HookContext context, Exception ex) {
        if (mqConsumerHooks == null || mqConsumerHooks.size() == 0) {
            return;
        }
        for (int i = this.hookIndex; i >= 0; i--) {
            MqConsumerHook consumerHook = mqConsumerHooks.get(i);
            try {
                consumerHook.afterCompletion(messageExt, context, ex);
            } catch (Throwable ex2) {
                log.error("MqConsumerHook.afterCompletion threw exception", ex2);
            }
        }
    }
}
