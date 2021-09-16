package com.github.raylin.mq.annotation;


import com.github.raylin.mq.common.MqConstant;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author lin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MqClient {

    /**
     * topic name
     */
    String topic() default "";

    /**
     * tag name
     */
    String tag() default MqConstant.NO_TAG;

    /**
     * 消费者组名称 required
     */
    String groupName() default "";

    /**
     * 最大消费者线程数
     */
    int maxThread() default 5;


    /**
     * 最小消费者线程数
     */
    int minThread() default 2;

    /**
     * 最大重新消费次数
     * <p>
     * In concurrently mode, -1 means 16;
     * In orderly mode, -1 means Integer.MAX_VALUE.
     */
    int maxReconsumeTimes() default -1;

    /**
     * 是否顺序消费
     */
    boolean orderly() default false;

    /**
     * 集群/广播消费
     */
    MessageModel messageModel() default MessageModel.CLUSTERING;

}
