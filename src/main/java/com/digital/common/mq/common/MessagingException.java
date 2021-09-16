package com.digital.common.mq.common;

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
