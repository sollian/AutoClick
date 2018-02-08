package com.sollian.autoclick.window;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.sollian.autoclick.Cache.ConfigCache;
import com.sollian.autoclick.MainActivity;
import com.sollian.autoclick.R;
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
    private final Spinner vSpeed;

    private boolean isAdded;

    private int statusbarHeight;

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

        vSpeed = vRoot.findViewById(R.id.speed);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,
                context.getResources().getStringArray(R.array.speed));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vSpeed.setAdapter(adapter);
        vSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        ConfigCache.getInstance().setTimeDelay(ConfigCache.SPEED_SLOW);
                        break;
                    case 1:
                        ConfigCache.getInstance().setTimeDelay(ConfigCache.SPEED_MIDDLE);
                        break;
                    case 2:
                        ConfigCache.getInstance().setTimeDelay(ConfigCache.SPEED_FAST);
                        break;
                    case 3:
                        ConfigCache.getInstance().setTimeDelay(ConfigCache.SPEED_VERY_FAST);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                vSpeed.setSelection(0);
            }
        });

        vSpeed.setSelection(getSpeedPos());
        statusbarHeight = Util.getStatusbarHeight(context);
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
                if (!isSameSpeed()) {
                    vSpeed.setSelection(getSpeedPos());
                }
                break;
            default:
                break;
        }
    }

    private boolean isSameSpeed() {
        int pos = vSpeed.getSelectedItemPosition();
        int timeDelay = ConfigCache.getInstance().getTimeDelay();
        return pos == 0 && timeDelay == ConfigCache.SPEED_SLOW
                || pos == 1 && timeDelay == ConfigCache.SPEED_MIDDLE
                || pos == 2 && timeDelay == ConfigCache.SPEED_FAST
                || pos == 3 && timeDelay == ConfigCache.SPEED_VERY_FAST;
    }

    private int getSpeedPos() {
        int timeDelay = ConfigCache.getInstance().getTimeDelay();
        int pos = 0;
        switch (timeDelay) {
            case ConfigCache.SPEED_SLOW:
                pos = 0;
                break;
            case ConfigCache.SPEED_MIDDLE:
                pos = 1;
                break;
            case ConfigCache.SPEED_FAST:
                pos = 2;
                break;
            case ConfigCache.SPEED_VERY_FAST:
                pos = 3;
                break;
            default:
                break;
        }
        return pos;
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ConfigCache.getInstance().setEnable(isChecked);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_root:
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                break;
            default:
                break;
        }
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