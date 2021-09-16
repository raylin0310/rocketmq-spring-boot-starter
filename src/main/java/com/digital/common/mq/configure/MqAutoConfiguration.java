package com.digital.common.mq.configure;

import com.digital.common.mq.core.*;
import com.digital.common.mq.hook.DefaultMqConsumerHookProvider;
import com.digital.common.mq.hook.MqConsumerHook;
import com.digital.common.mq.hook.MqConsumerHookProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author lin
 */
@Configuration
@EnableConfigurationProperties(MqProperties.class)
@ConditionalOnProperty(prefix = "rocketmq", value = "name-server")
@Import({MqListenerConfiguration.class})
public class MqAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(MessageConvert.class)
    public MessageConvert messageConvert() {
        return new JacksonMessageConvert();
    }

    @Bean
    @ConditionalOnMissingBean(MqConsumerHookProvider.class)
    public MqConsumerHookProvider mqConsumerHookProvider(List<MqConsumerHook> mqConsumerHooks) {
        return new DefaultMqConsumerHookProvider(mqConsumerHooks);
    }

    @Bean
    @ConditionalOnMissingBean(ConsumerGroupNameBuilder.class)
    public ConsumerGroupNameBuilder consumerGroupNameBuilder() {
        return new DefaultConsumerGroupNameBuilder();
    }

    @Bean
    public DispatcherTxListener dispatcherTxListener(List<TxMqListener> txMqListeners) {
        return new DispatcherTxListener(txMqListeners);
    }

    @Bean
    @ConditionalOnMissingBean(MqProducerExecutorServiceProvider.class)
    public MqProducerExecutorServiceProvider defaultMqProducerExecutorServiceProvider() {
        return new DefaultMqProducerExecutorServiceProvider();
    }

    @Bean
    public MqProducerProvider mqProducerProvider(MqProperties mqProperties,
                                                 DispatcherTxListener dispatcherTxListener,
                                                 MqProducerExecutorServiceProvider mqProducerExecutorServiceProvider) {
        MqProducerProvider mqProducerProvider = new MqProducerProvider();
        mqProducerProvider.setMqProperties(mqProperties);
        mqProducerProvider.setDispatcherTxListener(dispatcherTxListener);
        mqProducerProvider.setMqProducerExecutorServiceProvider(mqProducerExecutorServiceProvider);
        return mqProducerProvider;
    }

    @Bean
    public MqTemplate mqTemplate(MqProducerProvider producerProvider, MessageConvert messageConvert) {
        MqTemplate mqTemplate = new MqTemplate();
        mqTemplate.setProducer(producerProvider.createTransactionMQProducer());
        mqTemplate.setMessageConvert(messageConvert);
        return mqTemplate;
    }
}
