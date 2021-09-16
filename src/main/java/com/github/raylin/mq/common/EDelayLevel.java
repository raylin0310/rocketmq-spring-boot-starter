package com.github.raylin.mq.common;

/**
 * 延迟时间等级
 *
 * @author lin
 */

public enum EDelayLevel {

    //1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h

    _1S(1),
    _5S(2),
    _10S(3),
    _30S(4),
    _1M(5),
    _2M(6),
    _3M(7),
    _4M(8),
    _5M(9),
    _6M(10),
    _7M(11),
    _8M(12),
    _9M(13),
    _10M(14),
    _20M(15),
    _30M(16),
    _1H(17),
    _2H(18);
    private final int level;

    EDelayLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
