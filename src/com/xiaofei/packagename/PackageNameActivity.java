package com.xiaofei.packagename;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

@SuppressLint("NewApi")
public class PackageNameActivity extends Activity implements
		OnItemClickListener, LoaderManager.LoaderCallbacks<List<AppInfo>>,
		OnQueryTextListener, OnCloseListener {

	private final static String TAG = "com.xiaofei.packagename";

	private ListView mListView = null;

	private List<AppInfo> mApps = null;

	private AppInfoAdapter mAdapter = null;

	MySearchView mSearchView = null;

	int mIntentExtra = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_app_list);

		// We have a menu item to show in action bar.
		// setHasOptionsMenu(true);

		mListView = (ListView) findViewById(R.id.listviewApp);

		mApps = new ArrayList<AppInfo>();
		mAdapter = new AppInfoAdapter(this, mApps);

		Intent intent = getIntent();
		mIntentExtra = intent.getIntExtra("com.xiaofei.packagename.querytype",
				0);

		mListView.startAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));
		mListView.setVisibility(View.INVISIBLE);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		showDialog(0);

		// List<AppInfo> apps = new ArrayList<AppInfo>();
		// queryAppInfo(this, apps); // 查询所有应用程序信息
		// mAdapter.setData(apps);
		// dismissDialog(0);
		// mListView.startAnimation(AnimationUtils.loadAnimation(this,
		// android.R.anim.fade_in));
		// mListView.setVisibility(View.VISIBLE);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog dialog = null;

		if (id == 0) {
			dialog = new ProgressDialog(this);
			// dialog.setTitle("正在加载");
			dialog.setMessage("请稍等...");
			dialog.setIndeterminate(true);
			// dialog.setCancelable(true);
		}

		return dialog;
	}

	public static class MySearchView extends SearchView {
		public MySearchView(Context context) {
			super(context);
		}

		// The normal SearchView doesn't clear its search text when
		// collapsed, so we will do this for it.
		@Override
		public void onActionViewCollapsed() {
			setQuery("", false);
			super.onActionViewCollapsed();
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// Don't care about this.
		return true;
	}

	@Override
	public boolean onClose() {
		if (!TextUtils.isEmpty(mSearchView.getQuery())) {
			mSearchView.setQuery(null, true);
		}

		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// Called when the action bar search text has changed. Since this
		// is a simple array adapter, we can just have it do the filtering.
		String filter = !TextUtils.isEmpty(newText) ? newText : null;
		mAdapter.filter(filter);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!super.onCreateOptionsMenu(menu)) {
			return false;
		}

		// Place an action bar item for searching.
		MenuItem item = menu.add("Search");
		item.setIcon(android.R.drawable.ic_menu_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		mSearchView = new MySearchView(this);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setOnCloseListener(this);
		mSearchView.setIconifiedByDefault(true);
		item.setActionView(mSearchView);

		return true;
	}

	// 点击跳转至该应用程序
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long arg3) {
		// TODO Auto-generated method stub
		// Intent intent = adapter.getItem(position).getIntent();
		// startActivity(intent);
	}

	@Override
	public Loader<List<AppInfo>> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return new AppListLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<List<AppInfo>> loader, List<AppInfo> apps) {
		// TODO Auto-generated method stub

		Log.e(TAG, new Exception().getStackTrace()[0].toString() + "size: "
				+ apps.size());

		// Set the new data in the adapter.
		mAdapter.setData(apps);

		dismissDialog(0);

		mListView.startAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));

		// The list should now be shown.
		mListView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLoaderReset(Loader<List<AppInfo>> loader) {
		// TODO Auto-generated method stub

		Log.e(TAG, new Exception().getStackTrace()[0].toString());
		mAdapter.setData(null);
	}

	/**
	 * A custom Loader that loads all of the installed applications.
	 */
	public static class AppListLoader extends AsyncTaskLoader<List<AppInfo>> {
		List<AppInfo> mApps = null;

		PackageIntentReceiver mPackageObserver = null;

		int mIntentExtra = 0;

		public AppListLoader(Context context) {
			super(context);

			mIntentExtra = 0;
		}

		public AppListLoader(Context context, int intentExtra) {
			super(context);

			mIntentExtra = intentExtra;
		}

		/**
		 * This is where the bulk of our work is done. This function is called
		 * in a background thread and should generate a new set of data to be
		 * published by the loader.
		 */
		@Override
		public List<AppInfo> loadInBackground() {
			queryAppInfo(getContext(), mApps);
			Log.e(TAG, new Exception().getStackTrace()[0].toString() + "size: "
					+ mApps.size());

			return mApps;
		}

		/**
		 * Called when there is new data to deliver to the client. The super
		 * class will take care of delivering it; the implementation here just
		 * adds a little more logic.
		 */
		@Override
		public void deliverResult(List<AppInfo> apps) {
			if (isReset()) {
				// An async query came in while the loader is stopped. We
				// don't need the result.
				if (apps != null) {
					onReleaseResources(apps);
				}
			}

			if (isStarted()) {
				// If the Loader is currently started, we can immediately
				// deliver its results.
				super.deliverResult(apps);
			}
		}

		/**
		 * Handles a request to start the Loader.
		 */
		@Override
		protected void onStartLoading() {
			if (mApps != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mApps);
			}

			// Start watching for changes in the app data.
			if (mPackageObserver == null) {
				mPackageObserver = new PackageIntentReceiver(this);
			}

			if (takeContentChanged() || mApps == null) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				mApps = new ArrayList<AppInfo>();
				forceLoad();
			}
		}

		/**
		 * Handles a request to stop the Loader.
		 */
		@Override
		protected void onStopLoading() {
			// Attempt to cancel the current load task if possible.
			cancelLoad();
		}

		/**
		 * Handles a request to cancel a load.
		 */
		@Override
		public void onCanceled(List<AppInfo> apps) {
			super.onCanceled(apps);

			// At this point we can release the resources associated with 'apps'
			// if needed.
			onReleaseResources(apps);
		}

		/**
		 * Handles a request to completely reset the Loader.
		 */
		@Override
		protected void onReset() {
			super.onReset();

			// Ensure the loader is stopped
			onStopLoading();

			// At this point we can release the resources associated with 'apps'
			// if needed.
			if (mApps != null) {
				onReleaseResources(mApps);
				mApps = null;
			}

			// Stop monitoring for changes.
			if (mPackageObserver != null) {
				getContext().unregisterReceiver(mPackageObserver);
				mPackageObserver = null;
			}
		}

		/**
		 * Helper function to take care of releasing resources associated with
		 * an actively loaded data set.
		 */
		protected void onReleaseResources(List<AppInfo> apps) {
			// For a simple List<> there is nothing to do. For something
			// like a Cursor, we would close it here.
		}

		// 获得所有启动Activity的信息，类似于Launch界面
		public void queryAppInfo(Context context, List<AppInfo> apps) {
			switch (mIntentExtra) {
			case 0:
				getAppInfoByGetInstalledApplications(context, apps);
				break;

			case 1:
				getAppInfoByGetInstalledPackages(context, apps);
				break;

			case 2:
				getAppInfoByQueryIntentActivities(context, apps);
				break;

			default:
				getAppInfoByGetInstalledApplications(context, apps);
				break;
			}

		}

		public class ApplicationInfoComparator implements
				Comparator<ApplicationInfo> {
			private final Collator sCollator = Collator.getInstance();

			public final int compare(ApplicationInfo a, ApplicationInfo b) {
				return sCollator.compare(a.packageName.toString(),
						b.packageName.toString());
			}
		};

		public void getAppInfoByGetInstalledApplications(Context context,
				List<AppInfo> apps) {
			if (apps == null) {
				return;
			}

			apps.clear();

			PackageManager pm = context.getPackageManager(); // 获得PackageManager对象

			List<ApplicationInfo> listAppcations = pm
					.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
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
				return sCollator.compare(a.packageName.toString(),
						b.packageName.toString());
			}
		};

		public void getAppInfoByGetInstalledPackages(Context context,
				List<AppInfo> apps) {
			if (apps == null) {
				return;
			}

			apps.clear();

			PackageManager pm = context.getPackageManager(); // 获得PackageManager对象

			List<PackageInfo> packageInfoList = pm
					.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
			Collections.sort(packageInfoList, new PackageInfoComparator());

			for (PackageInfo info : packageInfoList) {
				String pkgName = info.packageName; // 获得应用程序的包名
				ApplicationInfo app;

				try {
					app = pm.getApplicationInfo(pkgName,
							PackageManager.GET_UNINSTALLED_PACKAGES);
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
				return sCollator.compare(a.activityInfo.packageName.toString(),
						b.activityInfo.packageName.toString());
			}
		};

		public void getAppInfoByQueryIntentActivities(Context context,
				List<AppInfo> apps) {
			if (apps == null) {
				return;
			}

			apps.clear();

			PackageManager pm = context.getPackageManager(); // 获得PackageManager对象

			// cat android/src/android/content/Intent.java | grep "public static
			// final String \s*\([^\s]\+\)\s*=\s*\"android.intent.category." |
			// sed
			// 's/public static final String \s*\([^\s
			// ]\+\)\s*=\s*\"android.intent.category.*/mainIntent.addCategory\(Intent.\1\);/g'
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			List<ResolveInfo> resolveInfos = pm.queryIntentActivities(
					mainIntent, 0);

			Collections.sort(resolveInfos, new ResolveInfoComparator());

			for (ResolveInfo reInfo : resolveInfos) {
				String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
				String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
				String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
				Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
				// 为应用程序的启动Activity 准备Intent
				Intent launchIntent = new Intent();
				launchIntent.setComponent(new ComponentName(pkgName,
						activityName));
				// 创建一个AppInfo对象，并赋值
				AppInfo appInfo = new AppInfo();
				appInfo.setAppLabel(appLabel);
				appInfo.setPkgName(pkgName);
				appInfo.setAppIcon(icon);
				appInfo.setIntent(launchIntent);
				apps.add(appInfo); // 添加至列表中
			}
		}

		// 根据packagename来卸载程序
		public void DeleteAppByActivityName(String packageName) {
			Intent deleteIntent = new Intent();
			deleteIntent.setAction(Intent.ACTION_DELETE);
			deleteIntent.setData(Uri.parse("package:" + packageName));
		}
	}

	public static class PackageIntentReceiver extends BroadcastReceiver {
		final AppListLoader mLoader;

		public PackageIntentReceiver(AppListLoader loader) {
			mLoader = loader;

			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);

			// Register for events related to sdcard installation.
			IntentFilter sdFilter = new IntentFilter();
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			mLoader.getContext().registerReceiver(this, sdFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

}
