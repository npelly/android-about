package org.npelly.android.about;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

import org.npelly.android.about.common.About;

public class MobileActivity extends AppCompatActivity {

    private static class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        @Override
        public int getCount() {
            return 4;
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return PackageListFragment.newInstance(PackageDetailAdapter.TYPE_PINNED);
                case 1: return PackageListFragment.newInstance(PackageDetailAdapter.TYPE_USER);
                case 2: return PackageListFragment.newInstance(PackageDetailAdapter.TYPE_SYSTEM);
                case 3: return PackageListFragment.newInstance(PackageDetailAdapter.TYPE_ALL);
                default: return null;
            }
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Starred Packages";
                case 1: return "User Installed Packages";
                case 2: return "System Installed Packages";
                case 3: return "All Packages";
                default: return null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        About.logd("MobileActivity onCreate()");

        setContentView(R.layout.activity_mobile);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setPageMargin(convertDip2Pixels(this, 16));
        viewPager.setAdapter(new MyPagerAdapter(getFragmentManager()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        About.logd("MobileActivity onDestroy()");
    }

    public static int convertDip2Pixels(Context context, int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                context.getResources().getDisplayMetrics());
    }
}
