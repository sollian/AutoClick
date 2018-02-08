package com.sollian.autoclick.Cache;

import android.graphics.Rect;

import com.sollian.autoclick.adapter.AppInfo;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author sollian on 2018/2/6.
 */

public final class ConfigCache {
    public static final int TYPE_PKG_NAME = 1;
    public static final int TYPE_TARGET = 2;
    public static final int TYPE_FREQUENCY = 3;
    public static final int TYPE_ENABLE = 4;
    public static final int TYPE_TIME_DELAY = 5;
    public static final int TYPE_SHUT_DOWN = 6;
    public static final int TYPE_COUNT = 7;

    private boolean isEnable;
    private AppInfo appInfo;
    private float frequency;
    private int timeDelay = 100;
    private final Rect targetRect = new Rect();
    private String targetClassName;
    private int totalCount;

    private final Collection<OnConfigChangeListener> listeners = new ArrayList<>();

    private ConfigCache() {
    }

    public static ConfigCache getInstance() {
        return Singleton.INSTANCE;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;

        notifyConfigChange(TYPE_ENABLE);
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        this.appInfo = appInfo;

        notifyConfigChange(TYPE_PKG_NAME);
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;

        notifyConfigChange(TYPE_FREQUENCY);
    }

    public int getTimeDelay() {
        return timeDelay;
    }

    public void setTimeDelay(int timeDelay) {
        this.timeDelay = timeDelay;

        notifyConfigChange(TYPE_TIME_DELAY);
    }

    public Rect getTargetRect() {
        return new Rect(targetRect);
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public void setTarget(Rect rect, String className) {
        if (rect != null) {
            targetRect.set(rect);
        } else {
            targetRect.set(0, 0, 0, 0);
        }
        targetClassName = className;

        notifyConfigChange(TYPE_TARGET);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void shutDown() {
        notifyConfigChange(TYPE_SHUT_DOWN);
    }

    private void notifyConfigChange(int type) {
        if (!listeners.isEmpty()) {
            for (OnConfigChangeListener listener : listeners) {
                listener.configChange(type);
            }
        }
    }

    public void register(OnConfigChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unRegister(OnConfigChangeListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public interface OnConfigChangeListener {
        void configChange(int type);
    }

    private static class Singleton {
        static final ConfigCache INSTANCE = new ConfigCache();
    }
}
