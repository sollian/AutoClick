package com.sollian.autoclick.Utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

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

    public static boolean isServiceStarted2(Context context, String serviceName) {
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
                    if (accessibilityService.equalsIgnoreCase(serviceName)) {
                        return true;
                    }

                }
            }
        }
        return false;
    }
}
