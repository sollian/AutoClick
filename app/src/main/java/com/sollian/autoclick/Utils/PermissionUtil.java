package com.sollian.autoclick.Utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;

import java.lang.reflect.Method;

/**
 * @author admin on 2018/2/8.
 */

public class PermissionUtil {
    public static boolean checkSysAlertWindow(Context context) {
        return checkOp(context, 24);
    }

    private static boolean checkOp(Context context, int op) {
        AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        try {
            Method method = AppOpsManager.class.getDeclaredMethod("checkOpNoThrow",
                    int.class, int.class, String.class);
            return AppOpsManager.MODE_ALLOWED == (int) method.invoke(
                    manager, op, Binder.getCallingUid(), context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}
