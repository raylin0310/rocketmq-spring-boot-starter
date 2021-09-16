package com.github.raylin.mq.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author lin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface TxMqClient {

    /**
     * topic name
     */
    String topic() default "";

    /**
     * tag name
     */
    String tag() default "";
}
