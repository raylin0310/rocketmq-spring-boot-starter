package com.github.raylin.mq.core;

import org.apache.rocketmq.client.producer.TransactionMQProducer;

/**
 * @author lin
 */
public class MqProducerProvider {

    private MqProperties mqProperties;
    private DispatcherTxListener dispatcherTxListener;
    private MqProducerExecutorServiceProvider mqProducerExecutorServiceProvider;

    public TransactionMQProducer createTransactionMQProducer() {
        MqProperties.Producer producerConfig = mqProperties.getProducer();
        TransactionMQProducer transactionMQProducer = new TransactionMQProducer(producerConfig.getGroup());
        transactionMQProducer.setNamesrvAddr(mqProperties.getNameServer());
        transactionMQProducer.setTransactionListener(dispatcherTxListener);
        //线程池
        transactionMQProducer.setExecutorService(mqProducerExecutorServiceProvider.buildExecutorService());
        transactionMQProducer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
        transactionMQProducer.setCompressMsgBodyOverHowmuch(producerConfig.getCompressMessageBodyThreshold());
        transactionMQProducer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
        transactionMQProducer.setRetryTimesWhenSendAsyncFailed(producerConfig.getRetryTimesWhenSendAsyncFailed());
        transactionMQProducer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());
        transactionMQProducer.setMaxMessageSize(producerConfig.getMaxMessageSize());
        return transactionMQProducer;
    }

    public MqProperties getMqProperties() {
        return mqProperties;
    }

    public void setMqProperties(MqProperties mqProperties) {
        this.mqProperties = mqProperties;
    }

    public DispatcherTxListener getDispatcherTxListener() {
        return dispatcherTxListener;
    }

    public void setDispatcherTxListener(DispatcherTxListener dispatcherTxListener) {
        this.dispatcherTxListener = dispatcherTxListener;
    }

    public MqProducerExecutorServiceProvider getMqProducerExecutorServiceProvider() {
        return mqProducerExecutorServiceProvider;
    }

    public void setMqProducerExecutorServiceProvider(MqProducerExecutorServiceProvider mqProducerExecutorServiceProvider) {
        this.mqProducerExecutorServiceProvider = mqProducerExecutorServiceProvider;
    }
}
