package org.npelly.android.about;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.npelly.android.about.common.About;
import org.npelly.android.about.common.PackageDetailManager;

/**
 * Handle package change intents.
 */
public class PackageChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String data = intent.getDataString();
        if (data.startsWith("package:")) {
            data = data.substring("package:".length());
        }
        About.logd("PackageChangeReceiver onReceive(action=%s, data=%s)", action, data);
        PackageDetailManager packageDetailManager = About.get().getPackageDetailManager();

        if (Intent.ACTION_PACKAGE_ADDED.equals(action) ||
                Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
                Intent.ACTION_PACKAGE_REPLACED.equals(action) ||
                Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            packageDetailManager.changePackage(data);
        }
    }
}
