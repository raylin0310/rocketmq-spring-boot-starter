package com.digital.common.mq.util;

import org.apache.rocketmq.common.UtilAll;

/**
 * @author lin
 */
public final class MqUtil {

    public static String getInstanceName(String identify) {
        char separator = '@';
        StringBuilder instanceName = new StringBuilder();
        instanceName.append(identify)
                .append(separator).append(UtilAll.getPid())
                .append(separator).append(System.nanoTime());
        return instanceName.toString();
    }
}
