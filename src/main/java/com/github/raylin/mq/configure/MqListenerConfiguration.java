package com.github.raylin.mq.configure;

import com.github.raylin.mq.annotation.MqClient;
import com.github.raylin.mq.core.*;
import com.github.raylin.mq.hook.MqConsumerHookProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author lin
 */
@Configuration
public class MqListenerConfiguration implements ApplicationContextAware, SmartInitializingSingleton {

    private final AtomicLong counter = new AtomicLong(0);

    private ConfigurableApplicationContext applicationContext;

    private final MqProperties mqProperties;

    private final MessageConvert messageConvert;

    private final MqConsumerHookProvider mqConsumerHookProvider;

    private final ConsumerGroupNameBuilder consumerGroupNameBuilder;

    public MqListenerConfiguration(MqProperties mqProperties, MessageConvert messageConvert,
                                   MqConsumerHookProvider mqConsumerHookProvider, ConsumerGroupNameBuilder consumerGroupNameBuilder) {
        this.mqProperties = mqProperties;
        this.messageConvert = messageConvert;
        this.mqConsumerHookProvider = mqConsumerHookProvider;
        this.consumerGroupNameBuilder = consumerGroupNameBuilder;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<MqListener> listeners = applicationContext.getBeansOfType(MqListener.class).values()
                .stream()
                .filter(e -> e.getClass().getAnnotation(MqClient.class) != null)
                .collect(Collectors.toList());
        if (listeners.size() == 0) {
            return;
        }
        listeners.forEach(this::register);
    }

    private void register(MqListener<?> mqListener) {

        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        String lionMqConsumerBeanName = String.format("%s_%s", LionMqConsumer.class.getName(),
                counter.incrementAndGet());
        genericApplicationContext.registerBean(lionMqConsumerBeanName, LionMqConsumer.class, () -> createLionMqConsumer(mqListener));
        LionMqConsumer lionMqConsumer = genericApplicationContext.getBean(lionMqConsumerBeanName, LionMqConsumer.class);
        if (!lionMqConsumer.isRunning()) {
            lionMqConsumer.start();
        }
    }

    private LionMqConsumer createLionMqConsumer(MqListener<?> mqListener) {
        LionMqConsumer consumer = new LionMqConsumer();
        consumer.setMqListener(mqListener);
        consumer.setMqClient(mqListener.getClass().getAnnotation(MqClient.class));
        consumer.setMqProperties(mqProperties);
        consumer.setMessageConvert(messageConvert);
        consumer.setMqConsumerHookProvider(mqConsumerHookProvider);
        consumer.setConsumerGroupNameBuilder(consumerGroupNameBuilder);
        return consumer;
    }


}
