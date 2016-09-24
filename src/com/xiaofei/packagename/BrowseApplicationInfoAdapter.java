package com.xiaofei.packagename;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BrowseApplicationInfoAdapter extends BaseAdapter {
	private final static String TAG = "com.xiaofei.packagename";

	private List<AppInfo> mApps = null;
	private List<AppInfo> mAppsLocal = null;

	LayoutInflater infater = null;

	public BrowseApplicationInfoAdapter(Context context, List<AppInfo> apps) {
		if (mAppsLocal == null) {
			mAppsLocal = new ArrayList<AppInfo>();
		}

		infater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mApps = apps;
		mAppsLocal.clear();
		mAppsLocal.addAll(apps);
	}

	public void setData(List<AppInfo> apps) {
		mApps.clear();
		mAppsLocal.clear();

		if (apps != null) {
			Log.e(TAG,
					new Exception().getStackTrace()[0].toString() + apps.size());
			mApps.addAll(apps);
			mAppsLocal.addAll(apps);
		}

		// notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int size = 0;

		if (mApps != null) {
			Log.e(TAG,
					new Exception().getStackTrace()[0].toString()
							+ mApps.size());
			// TODO Auto-generated method stub
			size = mApps.size();
		} else {
			Log.e(TAG, new Exception().getStackTrace()[0].toString()
					+ "mApps is null!");
		}

		return size;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		Object o = null;

		if (mApps.size() > 0) {
			o = mApps.get(position);
		}

		return o;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertview, ViewGroup arg2) {
		View view = null;
		ViewHolder holder = null;

		if (mApps.size() > 0) {
			if (convertview == null || convertview.getTag() == null) {
				view = infater.inflate(R.layout.browse_app_item, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				view = convertview;
				holder = (ViewHolder) convertview.getTag();
			}

			AppInfo appInfo = (AppInfo) getItem(position);
			holder.appIcon.setImageDrawable(appInfo.getAppIcon());
			holder.tvAppLabel.setText(appInfo.getAppLabel());
			holder.tvPkgName.setText(appInfo.getPkgName());
		}

		return view;
	}

	class ViewHolder {
		ImageView appIcon;
		TextView tvAppLabel;
		TextView tvPkgName;

		public ViewHolder(View view) {
			this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
			this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
			this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
		}
	}

	public void filter(String filter) {
		mApps.clear();

		if (filter == null) {
			mApps.addAll(mAppsLocal);
		} else {
			for (AppInfo appInfo : mAppsLocal) {
				// String pattern = filter.toString().toLowerCase();
				Pattern p = Pattern.compile(filter);
				Matcher matcher = p.matcher(appInfo.getAppLabel()
						+ appInfo.getPkgName());

				if (matcher.find()) {
					mApps.add(appInfo);
				}
			}
		}
	}
}
