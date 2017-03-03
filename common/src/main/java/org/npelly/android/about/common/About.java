package org.npelly.android.about.common;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

/**
 * God singleton to track & manage all active UI.
 */
public class About {
    public static final String TAG = "About";

    private static About singleton;

    public static void logw(String fmt, Object... args) {
        Log.w(TAG, String.format(fmt, args));
    }

    public static void logi(String fmt, Object... args) {
        Log.i(TAG, String.format(fmt, args));
    }

    public static void logd(String fmt, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format(fmt, args));
        }
    }

    public static void assertMainThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) throw new RuntimeException("Bad thread");
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
    private final PackageDetailManager packageDetailManager;
    private final PrefManager prefManager;

    private About(Context context) {
        this.context = context.getApplicationContext();
        prefManager = new PrefManager(this.context);
        packageDetailManager = new PackageDetailManager(this.context);
    }

    public PackageDetailManager getPackageDetailManager() {
        return packageDetailManager;
    }

    public Context getContext() {
        return context;
    }

    public PrefManager getPrefManager() {
        return prefManager;
    }
}
