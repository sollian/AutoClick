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
import com.sollian.autoclick.Utils.Speed;
import com.sollian.autoclick.adapter.AppInfo;

/**
 * @author sollian
 */
public class AutoClickService extends AccessibilityService implements ConfigCache.OnConfigChangeListener {
    private String pkgName;
    private Speed timeDelay = Speed.SLOW;

    private Rect rect;
    private String className;

    private boolean isEnable;

    private int times;
    private long start;

    private AccessibilityNodeInfo nodeInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.log("onCreate");
        ConfigCache.getInstance().register(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String pn = event.getPackageName().toString();
        if (!TextUtils.equals(pn, pkgName)) {
            switch (event.getEventType()) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    ConfigCache.getInstance().guide(ConfigCache.TYPE_GUIDE_EXIT_APP);
                    reset();
                    break;
                default:
                    break;
            }
            return;
        }

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                dealViewClick(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                ConfigCache.getInstance().guide(ConfigCache.TYPE_GUIDE_ENTER_APP);
                reset();
                break;
            default:
                break;
        }
    }

    private void dealViewClick(AccessibilityEvent event) {
        AccessibilityNodeInfo info = event.getSource();

        Rect bounds = new Rect();
        info.getBoundsInScreen(bounds);
        String clazzName = info.getClassName().toString();

        if (isEnable
                && bounds.equals(rect)
                && TextUtils.equals(clazzName, className)) {
            ConfigCache.getInstance().guide(ConfigCache.TYPE_GUIDE_START_AUTO_CLICK);
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
            ConfigCache.getInstance().guide(ConfigCache.TYPE_GUIDE_SEL_TARGET);
            ConfigCache.getInstance().setEnable(false);
            ConfigCache.getInstance().setTarget(bounds, clazzName);
            nodeInfo = info;
        }
    }

    private void reset() {
        ConfigCache.getInstance().setEnable(false);
        nodeInfo = null;
        ConfigCache.getInstance().setTarget(null, null);
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
        switch (type) {
            case ConfigCache.TYPE_PKG_NAME:
                start = 0;
                times = 0;
                pkgName = getPkgName();
                break;
            case ConfigCache.TYPE_TARGET:
                start = 0;
                times = 0;
                rect = ConfigCache.getInstance().getTargetRect();
                className = ConfigCache.getInstance().getTargetClassName();
                break;
            case ConfigCache.TYPE_ENABLE:
                start = 0;
                times = 0;
                isEnable = ConfigCache.getInstance().isEnable();
                if (isEnable && nodeInfo != null) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                break;
            case ConfigCache.TYPE_TIME_DELAY:
                start = 0;
                times = 0;
                timeDelay = ConfigCache.getInstance().getTimeDelay();
                updateServiceInfo();
                break;
            case ConfigCache.TYPE_SHUT_DOWN:
                start = 0;
                times = 0;
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
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = timeDelay.getTimeDelay();
        setServiceInfo(info);
    }
}