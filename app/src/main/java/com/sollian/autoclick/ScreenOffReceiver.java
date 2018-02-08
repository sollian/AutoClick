package com.sollian.autoclick;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sollian.autoclick.Cache.ConfigCache;

/**
 * @author sollian on 2018/2/6.
 */

public class ScreenOffReceiver extends BroadcastReceiver {
    private boolean isRegisterReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        if (action.equals(Intent.ACTION_SCREEN_ON)) {
//        } else
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            ConfigCache.getInstance().setEnable(false);
        }
    }

    public void registerScreenActionReceiver(Context mContext) {
        if (!isRegisterReceiver) {
            isRegisterReceiver = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
//            filter.addAction(Intent.ACTION_SCREEN_ON);
            mContext.registerReceiver(this, filter);
        }
    }

    public void unRegisterScreenActionReceiver(Context mContext) {
        if (isRegisterReceiver) {
            isRegisterReceiver = false;
            mContext.unregisterReceiver(this);
        }
    }
}