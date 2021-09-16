package com.digital.common.mq.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * @author lin
 */
public class JacksonMessageConvert implements MessageConvert {

    private final ObjectMapper objectMapper;

    public JacksonMessageConvert() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    public Object fromMessage(Message message, Class<?> targetClass) throws JsonProcessingException {
        String str = new String(message.getBody(), StandardCharsets.UTF_8);
        return objectMapper.readValue(str, targetClass);
    }

    @Override
    public byte[] toMessage(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new MessageConvertException("mq消息序列化异常", e);
        }
    }

    public static class MessageConvertException extends RuntimeException {
        public MessageConvertException(String msg) {
            super(msg);
        }

        public MessageConvertException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
