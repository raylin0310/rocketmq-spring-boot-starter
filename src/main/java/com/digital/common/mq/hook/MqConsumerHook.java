package com.digital.common.mq.hook;

import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author lin
 */
public interface MqConsumerHook {

    /**
     * 消息处理前
     *
     * @param messageExt msg
     * @param context    context
     * @return if success，then continue
     */
    default boolean preHandle(MessageExt messageExt, HookContext context) {
        return true;
    }

    /**
     * 消息处理后
     *
     * @param messageExt msg
     * @param context    context
     */
    void postHandle(MessageExt messageExt, HookContext context);

    /**
     * 完成之后
     *
     * @param messageExt msg
     * @param context    context
     * @param ex         exception
     */
    void afterCompletion(MessageExt messageExt, HookContext context, Exception ex);
}
