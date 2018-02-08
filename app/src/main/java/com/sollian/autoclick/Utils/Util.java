package com.sollian.autoclick.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

/**
 * @author sollian on 2018/2/6.
 */

public class Util {
//    public static boolean isServiceStarted(Context context, String serviceName) {
//        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
//        if (am == null) {
//            return false;
//        }
//        List<AccessibilityServiceInfo> serviceInfos = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
//        List<AccessibilityServiceInfo> installedAccessibilityServiceList = am.getInstalledAccessibilityServiceList();
//        for (AccessibilityServiceInfo info : installedAccessibilityServiceList) {
//            if (serviceName.equals(info.getId())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public static boolean isServiceStarted2(Context context) {
        int ok = 0;
        try {
            ok = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }

        if (ok == 1) {
            TextUtils.SimpleStringSplitter ms = new TextUtils.SimpleStringSplitter(':');
            String settingValue = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                ms.setString(settingValue);
                while (ms.hasNext()) {
                    String accessibilityService = ms.next();
                    if (accessibilityService.contains(context.getPackageName())) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getHeight();
    }

    public static boolean getHasShowGuide(Context context) {
        SharedPreferences sp = context.getSharedPreferences("sollian", Context.MODE_PRIVATE);
        return sp.getBoolean("showGuide", false);
    }

    public static void setHasShowGuide(Context context, boolean flag) {
        SharedPreferences sp = context.getSharedPreferences("sollian", Context.MODE_PRIVATE);
        sp.edit().putBoolean("showGuide", flag).apply();
    }

    public static int getStatusbarHeight(Context context) {
        int height = -1;
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }
}
