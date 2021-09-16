package com.github.raylin.mq.core;

import com.github.raylin.mq.annotation.MqClient;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author lin
 */
public class DefaultConsumerGroupNameBuilder implements ConsumerGroupNameBuilder, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public String build(MqClient mqClient) {
        String groupName = mqClient.groupName();
        if (groupName != null && !groupName.isEmpty()) {
            return groupName;
        }
        String appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (appName == null || appName.isEmpty()) {
            throw new IllegalArgumentException(mqClient.topic() + "请配置消费者groupName或者spring.application.name");
        }
        String topic = mqClient.topic();
        return appName + "_" + topic;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
