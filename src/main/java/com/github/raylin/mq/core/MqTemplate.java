package com.github.raylin.mq.core;

import com.github.raylin.mq.common.EDelayLevel;
import com.github.raylin.mq.common.MessagingException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

public class MqTemplate implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(MqTemplate.class);

    private TransactionMQProducer producer;
    private MessageConvert messageConvert;


    public void sendMessage(String topic, Object msg) {
        sendMessage(topic, "", msg);
    }

    public void sendMessage(String topic, String tag, Object msg) {
        Message message = new Message(topic, tag, messageConvert.toMessage(msg));
        sendMessage(message);
    }


    public void sendDelayMessage(String topic, Object msg, EDelayLevel delayLevel) {
        sendDelayMessage(topic, "", msg, delayLevel);
    }

    public void sendDelayMessage(String topic, String tag, Object msg, EDelayLevel delayLevel) {
        Message message = new Message(topic, tag, messageConvert.toMessage(msg));
        message.setDelayTimeLevel(delayLevel.getLevel());
        sendMessage(message);
    }


    public void sendOrderMessage(String topic, Object msg, Object key) {
        sendOrderMessage(topic, "", msg, key);
    }

    public void sendOrderMessage(String topic, String tag, Object msg, Object key) {
        Message message = new Message(topic, tag, messageConvert.toMessage(msg));
        try {
            this.producer.send(message, new SelectMessageQueueByHash(), key);
        } catch (Exception e) {
            throw new MessagingException("sendOrderMessage failed", e);
        }
    }

    public void sendMessageInTransaction(String topic, Object msg) {
        sendMessageInTransaction(topic, "", msg, null);
    }

    public void sendMessageInTransaction(String topic, Object msg, Object arg) {
        sendMessageInTransaction(topic, "", msg, arg);
    }

    public void sendMessageInTransaction(String topic, String tag, Object msg, Object arg) {
        Message message = new Message(topic, tag, messageConvert.toMessage(msg));
        try {
            this.producer.sendMessageInTransaction(message, arg);
        } catch (Exception e) {
            throw new MessagingException("sendOrderMessage failed", e);
        }
    }


    public void sendMessage(Message message) {
        try {
            this.producer.send(message);
        } catch (Exception e) {
            throw new MessagingException("sendMessage failed", e);
        }
    }


    @Override
    public void destroy() {
        if (Objects.nonNull(producer)) {
            producer.shutdown();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (producer != null) {
            producer.start();
        }
    }

    public TransactionMQProducer getProducer() {
        return producer;
    }

    public void setProducer(TransactionMQProducer producer) {
        this.producer = producer;
    }

    public MessageConvert getMessageConvert() {
        return messageConvert;
    }

    public void setMessageConvert(MessageConvert messageConvert) {
        this.messageConvert = messageConvert;
    }
}
