package com.sollian.autoclick.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.sollian.autoclick.Cache.ConfigCache;
import com.sollian.autoclick.R;

/**
 * @author sollian on 2018/2/6.
 */

public class FloatingMask implements ConfigCache.OnConfigChangeListener {
    private final Context mContext;
    private WindowManager.LayoutParams params;
    private final FloatingManager floatingManager;

    private final View vRoot;

    private boolean isAdded;

    public FloatingMask(Context context) {
        mContext = context.getApplicationContext();
        floatingManager = FloatingManager.getInstance(mContext);
        LayoutInflater mLayoutInflater = LayoutInflater.from(context);

        vRoot = mLayoutInflater.inflate(R.layout.float_mask, null);
    }

    public void show() {
        if (isAdded) {
            return;
        }
        params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;
        //总是出现在应用程序窗口之上
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
        params.width = 0;
        params.height = 0;
        isAdded = floatingManager.addView(vRoot, params);

        ConfigCache.getInstance().register(this);
    }

    public void hide() {
        if (!isAdded) {
            return;
        }
        isAdded = !floatingManager.removeView(vRoot);

        ConfigCache.getInstance().unRegister(this);
    }

    @Override
    public void configChange(int type) {
        switch (type) {
            case ConfigCache.TYPE_ENABLE:
                break;
            case ConfigCache.TYPE_TARGET:
                updateMask();
                break;
            default:
                break;
        }
    }

    private void updateMask() {
        Rect rect = ConfigCache.getInstance().getTargetRect();
        params.x = rect.left;
        params.y = rect.top;
        params.width = rect.width();
        params.height = rect.height();
        floatingManager.updateView(vRoot, params);
    }
}