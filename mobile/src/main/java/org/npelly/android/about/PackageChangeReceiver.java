package org.npelly.android.about;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.npelly.android.about.common.About;

/**
 * Handle package change intents.
 */
public class PackageChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action) ||
                Intent.ACTION_PACKAGE_CHANGED.equals(action) ||
                Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
                Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            About.logd("PackageChangeReceiver onReceive() %s %s", action, intent.getDataString());
            About.get().getTextManager().generateText();
        }
    }
}
