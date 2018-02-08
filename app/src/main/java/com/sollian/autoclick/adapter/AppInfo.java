package com.sollian.autoclick.adapter;

import android.graphics.drawable.Drawable;

/**
 * @author sollian on 2018/2/6.
 */

public class AppInfo {
    private String name;
    private Drawable icon;
    private String pkgName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }
}
