package com.sollian.autoclick.Utils;

/**
 * @author admin on 2018/2/8.
 */

public enum Speed {
    SLOW(100, "慢"),
    MIDDLE(70, "中"),
    FAST(40, "快"),
    VERY_FAST(10, "极快");

    private final int timeDelay;
    private final String desc;
    Speed(int timeDelay, String desc) {
        this.timeDelay = timeDelay;
        this.desc = desc;
    }

    public int getTimeDelay() {
        return timeDelay;
    }

    public String getDesc() {
        return desc;
    }
}
