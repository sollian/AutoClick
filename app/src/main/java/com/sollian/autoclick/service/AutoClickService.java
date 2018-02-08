package com.sollian.autoclick.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.sollian.autoclick.Cache.ConfigCache;
import com.sollian.autoclick.Utils.LogUtil;
import com.sollian.autoclick.adapter.AppInfo;

/**
 * @author sollian
 */
public class AutoClickService extends AccessibilityService implements ConfigCache.OnConfigChangeListener {
    private String pkgName;
    private int timeDelay = 100;

    private Rect rect;
    private String className;

    private boolean isEnable;

    private int times;
    private long start;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.log("onCreate");
        ConfigCache.getInstance().register(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo info = event.getSource();

        Rect bounds = new Rect();
        info.getBoundsInScreen(bounds);
        String clazzName = info.getClassName().toString();

        if (!isEnable) {
            ConfigCache.getInstance().setTarget(bounds, clazzName);
        } else {
            if (bounds.equals(rect) && TextUtils.equals(clazzName, className)) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                times++;
                if (start == 0) {
                    start = System.currentTimeMillis();
                } else {
                    float frequency = times * 1000.0f / (System.currentTimeMillis() - start);
                    ConfigCache.getInstance().setFrequency(frequency);
                }
                ConfigCache.getInstance().setTotalCount(times);
            } else {
                ConfigCache.getInstance().setEnable(false);
                ConfigCache.getInstance().setTarget(null, null);
            }
        }
    }

    @Override
    public void onInterrupt() {
        LogUtil.log("onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        LogUtil.log("onServiceConnected");
        pkgName = getPkgName();
        timeDelay = ConfigCache.getInstance().getTimeDelay();
        updateServiceInfo();
    }

    private String getPkgName() {
        AppInfo info = ConfigCache.getInstance().getAppInfo();
        if (info != null) {
            return info.getPkgName();
        } else {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.log("onDestroy");
        ConfigCache.getInstance().unRegister(this);
    }

    @Override
    public void configChange(int type) {
        if (type != ConfigCache.TYPE_FREQUENCY
                && type != ConfigCache.TYPE_COUNT) {
            start = 0;
            times = 0;
        }
        switch (type) {
            case ConfigCache.TYPE_PKG_NAME:
                pkgName = getPkgName();
                updateServiceInfo();
                break;
            case ConfigCache.TYPE_TARGET:
                rect = ConfigCache.getInstance().getTargetRect();
                className = ConfigCache.getInstance().getTargetClassName();
                break;
            case ConfigCache.TYPE_ENABLE:
                isEnable = ConfigCache.getInstance().isEnable();
                break;
            case ConfigCache.TYPE_TIME_DELAY:
                timeDelay = ConfigCache.getInstance().getTimeDelay();
                updateServiceInfo();
                break;
            case ConfigCache.TYPE_SHUT_DOWN:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    disableSelf();
                }
                break;
            default:
                break;
        }
    }

    private void updateServiceInfo() {
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            info = new AccessibilityServiceInfo();
        }
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (pkgName == null) {
            info.packageNames = null;
        } else {
            info.packageNames = new String[]{pkgName};
        }
        info.notificationTimeout = timeDelay;
        setServiceInfo(info);
    }
}