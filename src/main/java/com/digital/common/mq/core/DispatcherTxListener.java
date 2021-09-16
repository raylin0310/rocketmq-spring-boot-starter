package com.digital.common.mq.core;

import com.digital.common.mq.annotation.TxMqClient;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 事物消息调度
 *
 * @author lilin
 */
public class DispatcherTxListener implements TransactionListener {
    private final static Logger log = LoggerFactory.getLogger(LionMqConsumer.class);
    private static final String KEY_FORMAT = "%s_%s";

    private final Map<String, TxMqListener> callBackMap = new HashMap<>();

    public DispatcherTxListener(List<TxMqListener> txMqCallBacks) {
        if (txMqCallBacks == null || txMqCallBacks.isEmpty()) {
            return;
        }
        txMqCallBacks = txMqCallBacks.stream().filter(e -> e.getClass().getAnnotation(TxMqClient.class) != null).collect(Collectors.toList());
        for (TxMqListener txMqCallBack : txMqCallBacks) {
            TxMqClient annotation = txMqCallBack.getClass().getAnnotation(TxMqClient.class);
            String key = buildListenerKey(annotation, txMqCallBack);
            if (callBackMap.containsKey(key)) {
                throw new IllegalArgumentException("事物消息TxMqListener重复, key：" + key);
            }
            callBackMap.put(key, txMqCallBack);
        }
    }

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        TxMqListener txCallBack = getTxCallBack(msg);
        try {
            txCallBack.executeLocalTransaction(msg, arg);
        } catch (Exception ex) {
            log.error("执行本地事物异常，回滚事务消息，topic：{}", msg.getTopic(), ex);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
        return LocalTransactionState.COMMIT_MESSAGE;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        TxMqListener txCallBack = getTxCallBack(msg);
        try {
            boolean b = txCallBack.checkLocalTransaction(msg);
            return b ? LocalTransactionState.COMMIT_MESSAGE : LocalTransactionState.ROLLBACK_MESSAGE;
        } catch (Exception ex) {
            log.error("事务消息回查异常，topic:{}", msg.getTopic(), ex);
            return LocalTransactionState.UNKNOW;
        }
    }

    private TxMqListener getTxCallBack(Message msg) {
        String topic = msg.getTopic();
        String tags = Optional.ofNullable(msg.getTags()).orElse("");
        TxMqListener txMqCallBack = callBackMap.get(getListenerKey(topic, tags));
        if (txMqCallBack == null) {
            String emsg = String.format("topic：%s，tags：%s 发送事物消息时，不存在对应的TxMqListener", topic, tags);
            log.error(emsg);
            throw new IllegalArgumentException(emsg);
        }
        return txMqCallBack;
    }

    private String buildListenerKey(TxMqClient annotation, TxMqListener txMqCallBack) {
        return String.format(KEY_FORMAT, annotation.topic(), annotation.tag());
    }

    private String getListenerKey(String topic, String tags) {
        return String.format(KEY_FORMAT, topic, tags);
    }
}
