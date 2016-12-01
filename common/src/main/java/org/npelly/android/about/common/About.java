package org.npelly.android.about.common;

import android.content.Context;
import android.util.Log;

/**
 * God singleton to track & manage all active UI.
 */
public class About {
    public static final String TAG = "About";

    private static About singleton;

    public static void logd(String fmt, Object... args) {
        // TODO: figure out why buildConfigField is not working
        if (true) {
            Log.d(TAG, String.format(fmt, args));
        }
    }

    public static void createSingleton(Context context) {
        if (singleton == null) {
            singleton = new About(context);
        }
    }

    public static About get() {
        if (singleton == null) {
            throw new RuntimeException("Singleton requested before creation.");
        }
        return singleton;
    }

    private final Context context;  // Application context
    private final TextManager textManager;

    private About(Context context) {
        this.context = context.getApplicationContext();
        textManager = new TextManager(this.context);
//        packageManager = this.context.getPackageManager();
    }

    public TextManager getTextManager() {
        return textManager;
    }

    public Context getContext() {
        return context;
    }
}
