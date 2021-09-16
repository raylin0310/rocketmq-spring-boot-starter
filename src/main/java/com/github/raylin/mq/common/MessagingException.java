package com.github.raylin.mq.common;

/**
 * @author lin
 */
public class MessagingException extends RuntimeException {

    public MessagingException(String msg) {
        super(msg);
    }

    public MessagingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
