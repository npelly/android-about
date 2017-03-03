package org.npelly.android.about.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashSet;

public class PrefManager {
    private static final int CURRENT_VERSION = 1;

    private static final String PREF_PINNED_PACKAGES = "pinned_packages";
    private static final String PREF_VERSION = "version";

    private static final String[] DEFAULT_PINNED_PACKAGES = {
            "android",
            "com.google.android.gms",
            "com.google.android.googlequicksearchbox",
            "org.npelly.android.about",
    };

    private final SharedPreferences sharedPreferences;

    private HashSet<String> pinnedPackages = new HashSet<>();

    public PrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(About.TAG, Activity.MODE_PRIVATE);
        load();
    }

    public HashSet<String> getPinnedPackages() {
        return pinnedPackages;
    }

    public void addPinnedPackage(String packageName) {
        pinnedPackages.add(packageName);
        save();
    }

    public void removePinnedPackage(String packageName) {
        pinnedPackages.remove(packageName);
        save();
    }

    private void load() {
        About.logd("PrefManager loadPackages()");

        pinnedPackages.clear();
        int version = sharedPreferences.getInt(PREF_VERSION, 0);
        if (version < 1) {
            // PREF_PINNED_PACKAGES was introduced at version 1
            Collections.addAll(pinnedPackages, DEFAULT_PINNED_PACKAGES);
            About.logi("First run, created default pinned package list");
            save();
        } else {
            String packageList = sharedPreferences.getString(PREF_PINNED_PACKAGES, "");
            String[] packageNames = packageList.split(",");
            for (String name : packageNames) {
                if (!name.isEmpty()) {
                    pinnedPackages.add(name);
                }
            }
            About.logi("Loaded %d pinned packages", pinnedPackages.size());
        }

    }

    private void save() {
        About.logd("PrefManager savePackages() count=%d", pinnedPackages.size());

        String packageList = TextUtils.join(",", pinnedPackages);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_PINNED_PACKAGES, packageList);
        editor.putInt(PREF_VERSION, CURRENT_VERSION);
        editor.apply();
    }
}
