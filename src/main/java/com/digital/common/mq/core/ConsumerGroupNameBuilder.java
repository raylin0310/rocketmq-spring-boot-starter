package com.digital.common.mq.core;

import com.digital.common.mq.annotation.MqClient;


/**
 * @author lin
 */
public interface ConsumerGroupNameBuilder {

    /**
     * 生成消费者组名
     *
     * @param mqClient mqClient annotation
     * @return consumer group name
     */
    String build(MqClient mqClient);
}
