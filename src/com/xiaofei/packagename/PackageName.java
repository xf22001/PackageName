package com.xiaofei.packagename;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PackageName extends Activity {
    private final static String TAG = "com.xiaofei.packagename";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        bar.addTab(bar
                   .newTab()
                   .setText("installed app")
                   .setTabListener(
                       new TabListener<PackageNameFragmentActivity.AppListFragment>(
                           this,
                           "installed app",
                           PackageNameFragmentActivity.AppListFragment.class,
                           0)));
        bar.addTab(bar
                   .newTab()
                   .setText("installed package")
                   .setTabListener(
                       new TabListener<PackageNameFragmentActivity.AppListFragment>(
                           this,
                           "installed package",
                           PackageNameFragmentActivity.AppListFragment.class,
                           1)));
        bar.addTab(bar
                   .newTab()
                   .setText("installed activity")
                   .setTabListener(
                       new TabListener<PackageNameFragmentActivity.AppListFragment>(
                           this,
                           "installed activity",
                           PackageNameFragmentActivity.AppListFragment.class,
                           2)));

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

    public static class TabListener<T extends Fragment> implements
            ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz, int type) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = new Bundle();
            mArgs.putInt("type", type);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state. If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);

            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager()
                                         .beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        @SuppressLint("NewApi")
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(),
                                                 mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    }
}
