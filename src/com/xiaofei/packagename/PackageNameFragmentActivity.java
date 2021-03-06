package com.xiaofei.packagename;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

public class PackageNameFragmentActivity extends Activity {

	private final static String TAG = "com.xiaofei.packagename";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentManager fm = getFragmentManager();

		// Create the list fragment and add it as our sole content.
		if (fm.findFragmentById(android.R.id.content) == null) {
			AppListFragment list = new AppListFragment();
			fm.beginTransaction().add(android.R.id.content, list).commit();
		}
	}

	@SuppressLint("NewApi")
	public static class AppListFragment extends ListFragment implements
			OnQueryTextListener, OnCloseListener,
			LoaderManager.LoaderCallbacks<List<AppInfo>> {

		private List<AppInfo> mApps = null;

		private AppInfoAdapter mAdapter = null;

		MySearchView mSearchView = null;

		int mType = 0;

		static List<String> mCategoryList = null;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			mType = getArguments().getInt("type");

			// Give some text to display if there is no data. In a real
			// application this would come from a resource.
			setEmptyText("No applications");

			// We have a menu item to show in action bar.
			setHasOptionsMenu(true);

			mApps = new ArrayList<AppInfo>();
			// Create an empty adapter we will use to display the loaded data.
			mAdapter = new AppInfoAdapter(getActivity(), mApps);
			setListAdapter(mAdapter);

			// Start out with a progress indicator.
			setListShown(false);

			mCategoryList = new ArrayList<String>();

			// Prepare the loader. Either re-connect with an existing one,
			// or start a new one.
			if (mType == 1) {

				Dialog dialog = getMultiSelectDialog(0);
				dialog.show();
			} else {
				getLoaderManager().initLoader(0, null, this);
			}
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// Place an action bar item for searching.
			MenuItem item = menu.add("Search");
			item.setIcon(android.R.drawable.ic_menu_search);
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
					| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			mSearchView = new MySearchView(getActivity());
			mSearchView.setOnQueryTextListener(this);
			mSearchView.setOnCloseListener(this);
			mSearchView.setIconifiedByDefault(true);
			item.setActionView(mSearchView);
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
		public void onListItemClick(ListView l, View v, int position, long id) {
			// Insert desired behavior here.
			Log.i("LoaderCustom", "Item clicked: " + id);
		}

		@Override
		public Loader<List<AppInfo>> onCreateLoader(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub
			return new AppListLoader(getActivity(), mType);
		}

		@Override
		public void onLoadFinished(Loader<List<AppInfo>> loader,
				List<AppInfo> apps) {
			// TODO Auto-generated method stub

			Log.e(TAG, new Exception().getStackTrace()[0].toString() + "size: "
					+ apps.size());

			// Set the new data in the adapter.
			mAdapter.setData(apps);

			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}

		@Override
		public void onLoaderReset(Loader<List<AppInfo>> loader) {
			// TODO Auto-generated method stub

			Log.e(TAG, new Exception().getStackTrace()[0].toString());
			mAdapter.setData(null);
		}

		public class MySearchView extends SearchView {
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

		/**
		 * A custom Loader that loads all of the installed applications.
		 */
		public static class AppListLoader extends
				AsyncTaskLoader<List<AppInfo>> {
			List<AppInfo> mApps = null;
			int mType = 0;

			PackageIntentReceiver mPackageObserver = null;

			public AppListLoader(Context context) {
				super(context);

				mType = 0;
			}

			public AppListLoader(Context context, int type) {
				super(context);

				mType = type;
			}

			/**
			 * This is where the bulk of our work is done. This function is
			 * called in a background thread and should generate a new set of
			 * data to be published by the loader.
			 */
			@Override
			public List<AppInfo> loadInBackground() {
				try {
					queryAppInfo(getContext(), mType, mApps);
				} catch (Exception e) {
					// TODO: handle exception
					// e.printStackTrace();
				}
				Log.e(TAG, new Exception().getStackTrace()[0].toString()
						+ "size: " + mApps.size());

				return mApps;
			}

			/**
			 * Called when there is new data to deliver to the client. The super
			 * class will take care of delivering it; the implementation here
			 * just adds a little more logic.
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

				// At this point we can release the resources associated with
				// 'apps'
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

				// At this point we can release the resources associated with
				// 'apps'
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
			 * Helper function to take care of releasing resources associated
			 * with an actively loaded data set.
			 */
			protected void onReleaseResources(List<AppInfo> apps) {
				// For a simple List<> there is nothing to do. For something
				// like a Cursor, we would close it here.
			}

			// 获得所有启动Activity的信息，类似于Launch界面
			void queryAppInfo(Context context, int type, List<AppInfo> apps) {
				switch (type) {
				case 0:
					getAppInfoByGetInstalledApplications(context, apps);
					break;
				case 1:
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

			void getAppInfoByGetInstalledApplications(Context context,
					List<AppInfo> apps) {
				if (apps == null) {
					return;
				}

				apps.clear();

				PackageManager pm = context.getPackageManager(); // 获得PackageManager对象

				List<ApplicationInfo> listAppcations = pm
						.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
				Collections.sort(listAppcations,
						new ApplicationInfoComparator());

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

			public class ResolveInfoComparator implements
					Comparator<ResolveInfo> {
				private final Collator sCollator = Collator.getInstance();

				public final int compare(ResolveInfo a, ResolveInfo b) {
					return sCollator.compare(
							a.activityInfo.packageName.toString(),
							b.activityInfo.packageName.toString());
				}
			};

			void getAppInfoByQueryIntentActivities(Context context,
					List<AppInfo> apps) {
				if (apps == null) {
					return;
				}

				apps.clear();

				PackageManager pm = context.getPackageManager(); // 获得PackageManager对象

				// cat android/src/android/content/Intent.java | grep "public
				// static
				// final String \s*\([^\s]\+\)\s*=\s*\"android.intent.category."
				// |
				// sed
				// 's/public static final String \s*\([^\s
				// ]\+\)\s*=\s*\"android.intent.category.*/mainIntent.addCategory\(Intent.\1\);/g'
				Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);

				for (String category : mCategoryList) {
					mainIntent.addCategory(category);
				}

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

				IntentFilter filter = new IntentFilter(
						Intent.ACTION_PACKAGE_ADDED);
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

		class MultiChioceClickListener implements
				DialogInterface.OnMultiChoiceClickListener {
			public void onClick(DialogInterface dialog, int whichButton,
					boolean isChecked) {

				if (isChecked) {
					mCategoryList.add(values[whichButton]);
				} else {
					int location = mCategoryList.indexOf(values[whichButton]);

					if (location != -1) {
						mCategoryList.remove(location);
					}
				}

				/* User clicked on a check box do some stuff */
			}
		}

		class SingleChioceClickListener implements
				DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int whichButton) {

				mCategoryList.clear();
				mCategoryList.add(values[whichButton]);
				/* User clicked on a check box do some stuff */
			}
		}

		class OkClickListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int whichButton) {

				getLoaderManager().restartLoader(0, null, AppListFragment.this);
				/* User clicked Yes so do some stuff */
			}
		}

		class CancelClickListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int whichButton) {

				mCategoryList.clear();
				getLoaderManager().restartLoader(0, null, AppListFragment.this);
				/* User clicked Yes so do some stuff */
			}
		}

		String[] items = { "DEFAULT", "BROWSABLE", "ALTERNATIVE",
				"SELECTED_ALTERNATIVE", "TAB", "LAUNCHER", "INFO", "HOME",
				"PREFERENCE", "DEVELOPMENT_PREFERENCE", "EMBED", "APP_MARKET",
				"MONKEY", "TEST", "UNIT_TEST", "SAMPLE_CODE", "OPENABLE",
				"CAR_DOCK", "DESK_DOCK", "LE_DESK_DOCK", "HE_DESK_DOCK",
				"CAR_MODE", "APP_BROWSER", "APP_CALCULATOR", "APP_CALENDAR",
				"APP_CONTACTS", "APP_EMAIL", "APP_GALLERY", "APP_MAPS",
				"APP_MESSAGING", "APP_MUSIC", };

		String[] values = { Intent.CATEGORY_DEFAULT, Intent.CATEGORY_BROWSABLE,
				Intent.CATEGORY_ALTERNATIVE,
				Intent.CATEGORY_SELECTED_ALTERNATIVE, Intent.CATEGORY_TAB,
				Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_INFO,
				Intent.CATEGORY_HOME, Intent.CATEGORY_PREFERENCE,
				Intent.CATEGORY_DEVELOPMENT_PREFERENCE, Intent.CATEGORY_EMBED,
				Intent.CATEGORY_APP_MARKET, Intent.CATEGORY_MONKEY,
				Intent.CATEGORY_TEST, Intent.CATEGORY_UNIT_TEST,
				Intent.CATEGORY_SAMPLE_CODE, Intent.CATEGORY_OPENABLE,
				Intent.CATEGORY_CAR_DOCK, Intent.CATEGORY_DESK_DOCK,
				Intent.CATEGORY_LE_DESK_DOCK, Intent.CATEGORY_HE_DESK_DOCK,
				Intent.CATEGORY_CAR_MODE, Intent.CATEGORY_APP_BROWSER,
				Intent.CATEGORY_APP_CALCULATOR, Intent.CATEGORY_APP_CALENDAR,
				Intent.CATEGORY_APP_CONTACTS, Intent.CATEGORY_APP_EMAIL,
				Intent.CATEGORY_APP_GALLERY, Intent.CATEGORY_APP_MAPS,
				Intent.CATEGORY_APP_MESSAGING, Intent.CATEGORY_APP_MUSIC, };

		boolean checkedItems[] = new boolean[items.length];

		protected Dialog getMultiSelectDialog(int id) {
			Dialog dialog = null;

			switch (id) {
			case 0: {
				for (int i = 0; i < items.length; i++) {
					checkedItems[i] = false;
				}

				mCategoryList.clear();

				Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("过滤类别");
				builder.setMultiChoiceItems(items, checkedItems,
						new MultiChioceClickListener());
				builder.setPositiveButton("确定", new OkClickListener());
				builder.setNegativeButton("取消", new CancelClickListener());
				dialog = (Dialog) builder.create();
			}

				break;

			case 1: {
				mCategoryList.clear();

				Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("过滤类别");
				builder.setSingleChoiceItems(items, 0,
						new SingleChioceClickListener());
				builder.setPositiveButton("确定", new OkClickListener());
				builder.setNegativeButton("取消", new CancelClickListener());
				dialog = (Dialog) builder.create();
			}

				break;
			default:
				break;
			}

			return dialog;
		}

	}
}
