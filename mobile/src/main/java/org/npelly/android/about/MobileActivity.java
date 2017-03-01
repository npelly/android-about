package org.npelly.android.about;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.TextManager;

public class MobileActivity extends AppCompatActivity {

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;
        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return PinnedAppsFragment.newInstance();
                case 1: return AllAppsFragment.newInstance();
                default: return null;
            }
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return PinnedAppsFragment.getPageTitle();
                case 1: return AllAppsFragment.getPageTitle();
                default: return null;
            }
        }
    }

    FragmentPagerAdapter fragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        About.logd("MobileActivity onCreate()");

        setContentView(R.layout.activity_mobile);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        fragmentPagerAdapter = new MyPagerAdapter(getFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        About.logd("MobileActivity onDestroy()");
    }
}
