package com.github.raylin.mq.hook;

import java.util.HashMap;
import java.util.Map;

/**
 * hook上下文
 *
 * @author lin
 */
public class HookContext {

    private final Map<String, Object> attributes = new HashMap<>();

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
