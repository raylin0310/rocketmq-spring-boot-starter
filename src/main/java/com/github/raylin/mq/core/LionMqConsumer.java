package com.github.raylin.mq.core;

import com.github.raylin.mq.annotation.MqClient;
import com.github.raylin.mq.hook.HookContext;
import com.github.raylin.mq.hook.MqConsumerHookChain;
import com.github.raylin.mq.hook.MqConsumerHookProvider;
import com.github.raylin.mq.util.MqUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author lin
 */
public class LionMqConsumer implements SmartLifecycle, ApplicationContextAware, InitializingBean {

    private final static Logger log = LoggerFactory.getLogger(LionMqConsumer.class);

    private ApplicationContext applicationContext;

    private MessageConvert messageConvert;

    private volatile boolean running;

    private MqProperties mqProperties;

    private MqClient mqClient;

    private MqListener mqListener;

    private Type messageType;

    private DefaultMQPushConsumer consumer;

    private MqConsumerHookProvider mqConsumerHookProvider;

    private ConsumerGroupNameBuilder consumerGroupNameBuilder;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public synchronized void start() {
        if (this.isRunning()) {
            return;
        }
        try {
            consumer.start();
        } catch (MQClientException e) {
            throw new IllegalStateException("Failed to start RocketMQ push consumer", e);
        }
        this.running = true;
    }


    @Override
    public synchronized void stop() {
        if (this.isRunning()) {
            if (Objects.nonNull(consumer)) {
                consumer.shutdown();
            }
            this.running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        initPushConsumer();
        this.messageType = getMessageType();
    }


    private void initPushConsumer() throws MQClientException {
        Assert.notNull(mqListener, "property 'mqListener' is required");
        Assert.notNull(mqClient, "property 'mqConsumer' is required");
        Assert.isTrue(mqClient.maxThread() > mqClient.minThread(), "maxThread cant less minThread");

        String groupName = consumerGroupNameBuilder.build(mqClient);

        consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(mqProperties.getNameServer());
        consumer.setInstanceName(MqUtil.getInstanceName(mqProperties.getNameServer()));
        consumer.setConsumeThreadMax(mqClient.maxThread());
        consumer.setConsumeThreadMin(mqClient.minThread());
        consumer.setMaxReconsumeTimes(mqClient.maxReconsumeTimes());
        consumer.setMessageModel(mqClient.messageModel());
        consumer.subscribe(mqClient.topic(), mqClient.tag());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        if (mqClient.orderly()) {
            consumer.setMessageListener(new DefaultMessageListenerOrderly());
        } else {
            consumer.setMessageListener(new DefaultMessageListenerConcurrently());
        }
    }

    private Type getMessageType() {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(mqListener);
        Type matchedGenericInterface = null;
        while (Objects.nonNull(targetClass)) {
            Type[] interfaces = targetClass.getGenericInterfaces();
            if (Objects.nonNull(interfaces)) {
                for (Type type : interfaces) {
                    if (type instanceof ParameterizedType &&
                            (Objects.equals(((ParameterizedType) type).getRawType(), MqListener.class))) {
                        matchedGenericInterface = type;
                        break;
                    }
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        if (Objects.isNull(matchedGenericInterface)) {
            return Object.class;
        }

        Type[] actualTypeArguments = ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
        if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
            return actualTypeArguments[0];
        }
        return Object.class;
    }

    private void handleMessage(MessageExt messageExt) {
        mqListener.onMessage(doConvertMessage(messageExt));
    }

    private Object doConvertMessage(MessageExt messageExt) {
        if (Objects.equals(messageType, MessageExt.class) || Objects.equals(messageType, org.apache.rocketmq.common.message.Message.class)) {
            return messageExt;
        } else {
            if (Objects.equals(messageType, String.class)) {
                return new String(messageExt.getBody(), StandardCharsets.UTF_8);
            } else {
                try {
                    if (messageType instanceof Class) {
                        return this.getMessageConvert().fromMessage(messageExt, (Class<?>) messageType);
                    } else {
                        return this.getMessageConvert().fromMessage(messageExt, (Class<?>) ((ParameterizedType) messageType).getRawType());
                    }
                } catch (Exception e) {
                    log.info("convert failed. msgId:{}", messageExt.getMsgId());
                    throw new RuntimeException("cannot convert message to " + messageType, e);
                }
            }
        }
    }

    public class DefaultMessageListenerOrderly implements MessageListenerOrderly {

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
            for (MessageExt messageExt : msgs) {
                MqConsumerHookChain hookChain = mqConsumerHookProvider.getMqConsumerHookChain(messageExt);
                HookContext hookContext = new HookContext();
                try {
                    if (!hookChain.applyPreHandle(messageExt, hookContext)) {
                        continue;
                    }
                    handleMessage(messageExt);
                    hookChain.applyPostHandle(messageExt, hookContext);
                    hookChain.triggerAfterCompletion(messageExt, hookContext, null);
                } catch (Exception e) {
                    hookChain.triggerAfterCompletion(messageExt, hookContext, e);
                    log.warn("consume message failed. messageId:{}, topic:{}, reconsumeTimes:{}", messageExt.getMsgId(), messageExt.getTopic(), messageExt.getReconsumeTimes(), e);
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }
            return ConsumeOrderlyStatus.SUCCESS;
        }
    }

    public class DefaultMessageListenerConcurrently implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            for (MessageExt messageExt : msgs) {
                MqConsumerHookChain hookChain = mqConsumerHookProvider.getMqConsumerHookChain(messageExt);
                HookContext hookContext = new HookContext();
                try {
                    if (!hookChain.applyPreHandle(messageExt, hookContext)) {
                        continue;
                    }
                    handleMessage(messageExt);
                    hookChain.applyPostHandle(messageExt, hookContext);
                    hookChain.triggerAfterCompletion(messageExt, hookContext, null);
                } catch (Exception e) {
                    hookChain.triggerAfterCompletion(messageExt, hookContext, e);
                    log.warn("consume message failed. messageId:{}, topic:{}, reconsumeTimes:{}", messageExt.getMsgId(), messageExt.getTopic(), messageExt.getReconsumeTimes(), e);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

        }
    }

    public MqProperties getMqProperties() {
        return mqProperties;
    }

    public void setMqProperties(MqProperties mqProperties) {
        this.mqProperties = mqProperties;
    }

    public MqClient getMqClient() {
        return mqClient;
    }

    public void setMqClient(MqClient mqClient) {
        this.mqClient = mqClient;
    }

    public MqListener getMqListener() {
        return mqListener;
    }

    public void setMqListener(MqListener mqListener) {
        this.mqListener = mqListener;
    }

    public MessageConvert getMessageConvert() {
        return messageConvert;
    }

    public void setMessageConvert(MessageConvert messageConvert) {
        this.messageConvert = messageConvert;
    }

    public MqConsumerHookProvider getMqConsumerHookProvider() {
        return mqConsumerHookProvider;
    }

    public void setMqConsumerHookProvider(MqConsumerHookProvider mqConsumerHookProvider) {
        this.mqConsumerHookProvider = mqConsumerHookProvider;
    }

    public ConsumerGroupNameBuilder getConsumerGroupNameBuilder() {
        return consumerGroupNameBuilder;
    }

    public void setConsumerGroupNameBuilder(ConsumerGroupNameBuilder consumerGroupNameBuilder) {
        this.consumerGroupNameBuilder = consumerGroupNameBuilder;
    }
}
