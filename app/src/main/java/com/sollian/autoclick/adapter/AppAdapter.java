package com.sollian.autoclick.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sollian.autoclick.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sollian on 2018/2/6.
 */
public class AppAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final List<AppInfo> data = new ArrayList<>();

    public AppAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        getAppInfos();
    }

    private void getAppInfos() {
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);

        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //系统应用
                continue;
            }

            AppInfo tmpInfo = new AppInfo();
            tmpInfo.setName(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
            tmpInfo.setIcon(packageInfo.applicationInfo.loadIcon(context.getPackageManager()));
            tmpInfo.setPkgName(packageInfo.packageName);
            data.add(tmpInfo);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item, parent, false);
        }

        AppInfo info = getItem(position);
        ImageView vIcon = convertView.findViewById(R.id.icon);
        TextView vName = convertView.findViewById(R.id.name);
        vIcon.setImageDrawable(info.getIcon());
        vName.setText(info.getName());

        return convertView;
    }
}
