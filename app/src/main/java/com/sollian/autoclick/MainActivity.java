package com.sollian.autoclick;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sollian.autoclick.Cache.ConfigCache;
import com.sollian.autoclick.Utils.PermissionUtil;
import com.sollian.autoclick.Utils.Util;
import com.sollian.autoclick.adapter.AppAdapter;
import com.sollian.autoclick.adapter.AppInfo;
import com.sollian.autoclick.window.FloatingController;
import com.sollian.autoclick.window.FloatingMask;

public class MainActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, DialogInterface.OnClickListener {
    private Button vSwitch;
    private TextView vState;
    private RadioGroup vRadioGroup;

    private View vAppRoot;
    private ImageView vAppIcon;
    private TextView vAppName;

    private View vPermission;
    private TextView vPermissionState;

    private AlertDialog appsDialog;

    private ScreenOffReceiver receiver;

    private final Handler handler = new Handler();

    private FloatingController floatingController;
    private FloatingMask floatingMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        receiver = new ScreenOffReceiver();
        receiver.registerScreenActionReceiver(this);

        checkShowFloatIfNeed();
    }

    private void init() {
        vSwitch = findViewById(R.id.btn_switch);
        vState = findViewById(R.id.state);

        vRadioGroup = findViewById(R.id.speed);

        vAppRoot = findViewById(R.id.app_root);
        vAppIcon = findViewById(R.id.app_icon);
        vAppName = findViewById(R.id.app_name);

        vPermission = findViewById(R.id.permission);
        vPermissionState = findViewById(R.id.permission_state);

        vSwitch.setOnClickListener(this);
        vAppRoot.setOnClickListener(this);

        vRadioGroup.setOnCheckedChangeListener(this);

        updateSwitch();
        updateAppInfo();
        updatePermission();
    }

    private void checkShowFloatIfNeed() {
        do {
            if (!Util.isServiceStarted2(this)) {
                break;
            }

            if (ConfigCache.getInstance().getAppInfo() == null) {
                break;
            }

            if (floatingController == null) {
                floatingController = new FloatingController(this);
                floatingMask = new FloatingMask(this);
            }
            floatingMask.show();
            floatingController.show();
            return;
        } while (false);

        if (floatingController != null) {
            floatingController.hide();
            floatingMask.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSwitch();
        updatePermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.unRegisterScreenActionReceiver(this);
        handler.removeCallbacksAndMessages(null);

        if (floatingController != null) {
            floatingController.hide();
            floatingMask.hide();
        }
    }

    private void updateSwitch() {
        if (Util.isServiceStarted2(this)) {
            vState.setText("服务已开启");
            vSwitch.setText("关闭服务");
        } else {
            vState.setText("服务未开启");
            vSwitch.setText("开启服务");
        }

        checkShowFloatIfNeed();
    }

    private void updatePermission() {
        if (PermissionUtil.checkSysAlertWindow(this)) {
            vPermissionState.setText("悬浮窗权限已开启");
            vPermission.setVisibility(View.GONE);
        } else {
            vPermissionState.setText("悬浮窗权限未开启");
            vPermission.setVisibility(View.VISIBLE);
            vPermission.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                toggleService();
                break;
            case R.id.app_root:
                showAppsDialog();
                break;
            case R.id.permission:
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void showAppsDialog() {
        if (appsDialog == null) {
            appsDialog = new AlertDialog.Builder(this)
                    .setTitle("选择目标应用")
                    .setAdapter(new AppAdapter(this), this)
                    .create();
            appsDialog.setCancelable(true);
            appsDialog.setCanceledOnTouchOutside(true);
        }

        if (!appsDialog.isShowing()) {
            appsDialog.show();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int timeDelay = 100;
        switch (checkedId) {
            case R.id.slow:
                timeDelay = 100;
                break;
            case R.id.middle:
                timeDelay = 70;
                break;
            case R.id.fast:
                timeDelay = 40;
                break;
            case R.id.very_fast:
                timeDelay = 10;
                break;
            default:
                break;
        }
        ConfigCache.getInstance().setTimeDelay(timeDelay);
    }

    private void toggleService() {
        if (!Util.isServiceStarted2(this)) {
            //打开系统设置中辅助功能
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ConfigCache.getInstance().shutDown();
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSwitch();
                    }
                }, 1000);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        }
        updateSwitch();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        appsDialog.dismiss();
        AppInfo info = (AppInfo) appsDialog.getListView().getAdapter().getItem(which);
        ConfigCache.getInstance().setAppInfo(info);
        updateAppInfo();

        checkShowFloatIfNeed();
    }

    private void updateAppInfo() {
        AppInfo info = ConfigCache.getInstance().getAppInfo();

        if (info == null) {
            vAppIcon.setImageDrawable(null);
            vAppName.setText(null);
        } else {
            vAppName.setText(info.getName());
            vAppIcon.setImageDrawable(info.getIcon());
        }
    }
}
