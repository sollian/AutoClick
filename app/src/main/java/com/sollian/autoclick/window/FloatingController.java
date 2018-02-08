package com.sollian.autoclick.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.sollian.autoclick.Cache.ConfigCache;
import com.sollian.autoclick.R;
import com.sollian.autoclick.Utils.Speed;
import com.sollian.autoclick.Utils.Util;
import com.sollian.autoclick.adapter.AppInfo;

/**
 * @author sollian on 2018/2/6.
 */

public class FloatingController implements ConfigCache.OnConfigChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private final Context context;
    private WindowManager.LayoutParams params;
    private final FloatingManager floatingManager;

    private final View vRoot;
    private final View vAppRoot;
    private final ImageView vIcon;
    private final TextView vName;
    private final Switch vSwitch;
    private final TextView vFrequency;
    private final TextView vCount;

    private final View vDeSpeed;
    private final TextView vSpeed;
    private final View vInSpeed;

    private boolean isAdded;

    private int statusbarHeight;

    private Speed timeDelay;

    public FloatingController(Context context) {
        this.context = context.getApplicationContext();
        floatingManager = FloatingManager.getInstance(this.context);
        LayoutInflater mLayoutInflater = LayoutInflater.from(context);

        vRoot = mLayoutInflater.inflate(R.layout.float_controller, null);
        vRoot.setOnTouchListener(new MyTouchListener());

        vAppRoot = vRoot.findViewById(R.id.app_root);
        vIcon = vRoot.findViewById(R.id.icon);
        vName = vRoot.findViewById(R.id.name);
        vSwitch = vRoot.findViewById(R.id.service_switch);
        vFrequency = vRoot.findViewById(R.id.frequency);
        vCount = vRoot.findViewById(R.id.count);
        vAppRoot.setOnClickListener(this);
        vSwitch.setOnCheckedChangeListener(this);

        statusbarHeight = Util.getStatusbarHeight(context);

        vDeSpeed = vRoot.findViewById(R.id.deSpeed);
        vSpeed = vRoot.findViewById(R.id.speed);
        vInSpeed = vRoot.findViewById(R.id.inSpeed);
        vDeSpeed.setOnClickListener(this);
        vInSpeed.setOnClickListener(this);
        timeDelay = ConfigCache.getInstance().getTimeDelay();
        updateSpeed();
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
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        isAdded = floatingManager.addView(vRoot, params);

        ConfigCache.getInstance().register(this);
        updateAppInfo();
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
                if (vSwitch.isChecked() != ConfigCache.getInstance().isEnable()) {
                    vSwitch.setChecked(ConfigCache.getInstance().isEnable());
                }
                break;
            case ConfigCache.TYPE_PKG_NAME:
                updateAppInfo();
                break;
            case ConfigCache.TYPE_FREQUENCY:
                vFrequency.setText(String.format("%.1f次/s", ConfigCache.getInstance().getFrequency()));
                vCount.setText(String.valueOf(ConfigCache.getInstance().getTotalCount()));
                break;
            case ConfigCache.TYPE_TARGET:
                updateSwitchState();
                break;
            case ConfigCache.TYPE_TIME_DELAY:
                if (timeDelay != ConfigCache.getInstance().getTimeDelay()) {
                    timeDelay = ConfigCache.getInstance().getTimeDelay();
                    updateSpeed();
                }
                break;
            default:
                break;
        }
    }

    private void updateSwitchState() {
        if (!TextUtils.isEmpty(ConfigCache.getInstance().getTargetClassName())
                && !ConfigCache.getInstance().getTargetRect().isEmpty()) {
            vSwitch.setEnabled(true);
        } else {
            vSwitch.setEnabled(false);
        }
    }

    private void updateAppInfo() {
        AppInfo info = ConfigCache.getInstance().getAppInfo();
        if (info == null) {
            vIcon.setImageDrawable(null);
            vName.setText(null);
        } else {
            vIcon.setImageDrawable(info.getIcon());
            vName.setText(info.getName());
        }
    }

    private void updateSpeed() {
        vSpeed.setText(timeDelay.getDesc());
        vDeSpeed.setEnabled(true);
        vInSpeed.setEnabled(true);
        if (timeDelay == Speed.SLOW) {
            vDeSpeed.setEnabled(false);
        } else if (timeDelay == Speed.VERY_FAST) {
            vInSpeed.setEnabled(false);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ConfigCache.getInstance().setEnable(isChecked);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_root:
                Util.startActivity(context, ConfigCache.getInstance().getAppInfo().getPkgName());
                break;
            case R.id.deSpeed:
                changeSpeed(false);
                break;
            case R.id.inSpeed:
                changeSpeed(true);
                break;
            default:
                break;
        }
    }

    private void changeSpeed(boolean increase) {
        Speed[] speeds = Speed.values();
        int index = timeDelay.ordinal();
        if (increase) {
            if (index >= speeds.length - 1) {
                return;
            }
            timeDelay = speeds[index + 1];
        } else {
            if (index <= 0) {
                return;
            }
            timeDelay = speeds[index - 1];
        }
        ConfigCache.getInstance().setTimeDelay(timeDelay);
        updateSpeed();
    }

    private class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int offsetX = vRoot.getWidth() >> 1;
            int offsetY = vRoot.getHeight() >> 1;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    params.x = (int) event.getRawX() - offsetX;
                    params.y = (int) event.getRawY() - offsetY - statusbarHeight;
                    floatingManager.updateView(vRoot, params);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}