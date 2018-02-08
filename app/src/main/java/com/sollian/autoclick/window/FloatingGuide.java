package com.sollian.autoclick.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;

import com.sollian.autoclick.Cache.ConfigCache;
import com.sollian.autoclick.R;
import com.sollian.autoclick.Utils.Util;

/**
 * @author sollian on 2018/2/6.
 */

public class FloatingGuide implements ConfigCache.OnConfigChangeListener {
    private final Context context;
    private WindowManager.LayoutParams params;
    private final FloatingManager floatingManager;

    private final TextView vText;

    private boolean isAdded;

    private enum State {
        NONE, APP_OPENED, TARGET_SELECTED
    }

    private State state = State.NONE;

    public FloatingGuide(Context context) {
        this.context = context.getApplicationContext();
        floatingManager = FloatingManager.getInstance(this.context);
        LayoutInflater mLayoutInflater = LayoutInflater.from(context);

        vText = (TextView) mLayoutInflater.inflate(R.layout.float_guide, null);
    }

    public void show() {
        if (isAdded || Util.getHasShowGuide(context)) {
            return;
        }
        params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = (int) (Util.getScreenHeight(context) * 0.8);
        //总是出现在应用程序窗口之上
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置图片格式，效果为背景透明
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        isAdded = floatingManager.addView(vText, params);
        updateText();

        ConfigCache.getInstance().register(this);
    }

    public void hide() {
        if (!isAdded) {
            return;
        }
        isAdded = !floatingManager.removeView(vText);

        ConfigCache.getInstance().unRegister(this);
    }

    @Override
    public void configChange(int type) {
        switch (type) {
            case ConfigCache.TYPE_GUIDE_ENTER_APP:
                if (state == State.NONE
                        || state == State.TARGET_SELECTED) {
                    state = State.APP_OPENED;
                    updateText();
                }
                break;
            case ConfigCache.TYPE_GUIDE_SEL_TARGET:
                if (state == State.APP_OPENED) {
                    state = State.TARGET_SELECTED;
                    updateText();
                }
                break;
            case ConfigCache.TYPE_GUIDE_START_AUTO_CLICK:
                Util.setHasShowGuide(context, true);
                hide();
                break;
            case ConfigCache.TYPE_GUIDE_EXIT_APP:
                state = State.NONE;
                updateText();
                break;
            default:
                break;
        }
    }

    private void updateText() {
        switch (state) {
            case NONE:
                vText.setText("请打开\n" + ConfigCache.getInstance().getAppInfo().getName());
                break;
            case APP_OPENED:
                vText.setText("请选择\n需要自动点击的目标");
                break;
            case TARGET_SELECTED:
                vText.setText("点击悬浮窗的开关\n开始自动点击");
                break;
            default:
                break;
        }
    }
}