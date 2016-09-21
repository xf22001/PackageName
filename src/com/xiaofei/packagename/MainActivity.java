package com.xiaofei.packagename;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener, LoaderManager.LoaderCallbacks<List<AppEntry>> {

    private ListView listview = null;

    private List<AppInfo> mlistAppInfo = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_app_list);

        listview = (ListView) findViewById(R.id.listviewApp);
        mlistAppInfo = new ArrayList<AppInfo>();
        queryAppInfo(); // 查询所有应用程序信息
        BrowseApplicationInfoAdapter browseAppAdapter = new BrowseApplicationInfoAdapter(this, mlistAppInfo);
        listview.setAdapter(browseAppAdapter);
        listview.setOnItemClickListener(this);
    }

    // 点击跳转至该应用程序
    public void onItemClick(AdapterView<?> arg0, View view, int position,
                            long arg3) {
        // TODO Auto-generated method stub
        // Intent intent = mlistAppInfo.get(position).getIntent();
        // startActivity(intent);
    }

    // 获得所有启动Activity的信息，类似于Launch界面
    public void queryAppInfo() {
	    getAppInfoByGetInstalledApplications(this, mlistAppInfo);
	    //getAppInfoByGetInstalledPackages(this, mlistAppInfo);
	    //getAppInfoByQueryIntentActivities(this, mlistAppInfo);
    }

    public class ApplicationInfoComparator implements Comparator<ApplicationInfo> {
        private final Collator sCollator = Collator.getInstance();

        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            return sCollator.compare(a.packageName.toString(), b.packageName.toString());
        }
    };

    public void getAppInfoByGetInstalledApplications(Context context, List<AppInfo> apps) {
        if(apps == null) {
            return;
        }

        apps.clear();

        PackageManager pm = context.getPackageManager(); // 获得PackageManager对象

        List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
        Collections.sort(listAppcations, new ApplicationInfoComparator());

        for (ApplicationInfo app : listAppcations) {
            // 获得该应用程序的启动Activity的name
            String pkgName = app.packageName; // 获得应用程序的包名
            String appLabel = (String) app.loadLabel(pm); // 获得应用程序的Label
            Drawable icon = app.loadIcon(pm); // 获得应用程序图标
            // 创建一个AppInfo对象，并赋值
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLabel(appLabel);
            appInfo.setPkgName(pkgName);
            appInfo.setAppIcon(icon);
            apps.add(appInfo); // 添加至列表中
        }
    }

    public class PackageInfoComparator implements Comparator<PackageInfo> {
        private final Collator sCollator = Collator.getInstance();

        public final int compare(PackageInfo a, PackageInfo b) {
            return sCollator.compare(a.packageName.toString(), b.packageName.toString());
        }
    };

    public void getAppInfoByGetInstalledPackages(Context context, List<AppInfo> apps) {
        if(apps == null) {
            return;
        }

        apps.clear();

        PackageManager pm = context.getPackageManager(); // 获得PackageManager对象

        List<PackageInfo> packageInfoList = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);//GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
        Collections.sort(packageInfoList, new PackageInfoComparator());

        for (PackageInfo info : packageInfoList) {
            String pkgName = info.packageName; // 获得应用程序的包名
            ApplicationInfo app;

            try {
                app = pm.getApplicationInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }

            String appLabel = (String) app.loadLabel(pm); // 获得应用程序的Label
            Drawable icon = app.loadIcon(pm); // 获得应用程序图标
            // 创建一个AppInfo对象，并赋值
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLabel(appLabel);
            appInfo.setPkgName(pkgName);
            appInfo.setAppIcon(icon);
            apps.add(appInfo); // 添加至列表中
        }
    }

    public class ResolveInfoComparator implements Comparator<ResolveInfo> {
        private final Collator sCollator = Collator.getInstance();

        public final int compare(ResolveInfo a, ResolveInfo b) {
            return sCollator.compare(a.activityInfo.packageName.toString(), b.activityInfo.packageName.toString());
        }
    };

    public void getAppInfoByQueryIntentActivities(Context context, List<AppInfo> apps) {
        if(apps == null) {
            return;
        }

        apps.clear();

        PackageManager pm = context.getPackageManager(); // 获得PackageManager对象

        //cat android/src/android/content/Intent.java | grep "public static final String \s*\([^\s]\+\)\s*=\s*\"android.intent.category." | sed 's/public static final String \s*\([^\s ]\+\)\s*=\s*\"android.intent.category.*/mainIntent.addCategory\(Intent.\1\);/g'
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        //mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        //mainIntent.addCategory(Intent.CATEGORY_DEFAULT);
        //mainIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        //mainIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        //mainIntent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
        //mainIntent.addCategory(Intent.CATEGORY_TAB);
        //mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        //mainIntent.addCategory(Intent.CATEGORY_INFO);
        //mainIntent.addCategory(Intent.CATEGORY_HOME);
        //mainIntent.addCategory(Intent.CATEGORY_PREFERENCE);
        //mainIntent.addCategory(Intent.CATEGORY_DEVELOPMENT_PREFERENCE);
        //mainIntent.addCategory(Intent.CATEGORY_EMBED);
        //mainIntent.addCategory(Intent.CATEGORY_APP_MARKET);
        //mainIntent.addCategory(Intent.CATEGORY_MONKEY);
        //mainIntent.addCategory(Intent.CATEGORY_TEST);
        //mainIntent.addCategory(Intent.CATEGORY_UNIT_TEST);
        //mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);
        //mainIntent.addCategory(Intent.CATEGORY_OPENABLE);
        //mainIntent.addCategory(Intent.CATEGORY_CAR_DOCK);
        //mainIntent.addCategory(Intent.CATEGORY_DESK_DOCK);
        //mainIntent.addCategory(Intent.CATEGORY_LE_DESK_DOCK);
        //mainIntent.addCategory(Intent.CATEGORY_HE_DESK_DOCK);
        //mainIntent.addCategory(Intent.CATEGORY_CAR_MODE);
        //mainIntent.addCategory(Intent.CATEGORY_APP_BROWSER);
        //mainIntent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
        //mainIntent.addCategory(Intent.CATEGORY_APP_CALENDAR);
        //mainIntent.addCategory(Intent.CATEGORY_APP_CONTACTS);
        //mainIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
        //mainIntent.addCategory(Intent.CATEGORY_APP_GALLERY);
        //mainIntent.addCategory(Intent.CATEGORY_APP_MAPS);
        //mainIntent.addCategory(Intent.CATEGORY_APP_MESSAGING);
        //mainIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);

        Collections.sort(resolveInfos, new ResolveInfoComparator());

        for (ResolveInfo reInfo : resolveInfos) {
            String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
            String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
            String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
            Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
            // 为应用程序的启动Activity 准备Intent
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(pkgName, activityName));
            // 创建一个AppInfo对象，并赋值
            AppInfo appInfo = new AppInfo();
            appInfo.setAppLabel(appLabel);
            appInfo.setPkgName(pkgName);
            appInfo.setAppIcon(icon);
            appInfo.setIntent(launchIntent);
            mlistAppInfo.add(appInfo); // 添加至列表中
        }
    }

    //根据packagename来卸载程序
    public void DeleteAppByActivityName(String packageName) {
        Intent deleteIntent = new Intent();
        deleteIntent.setAction(Intent.ACTION_DELETE);
        deleteIntent.setData(Uri.parse("package:" + packageName));
    }
}
