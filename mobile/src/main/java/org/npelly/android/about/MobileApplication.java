package org.npelly.android.about;

import android.app.Application;

import org.npelly.android.about.common.About;

/**
 * Obtain application context before any other Android lifecycle events.
 */
public class MobileApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        About.logd("MobileApplication onCreate()");
        About.createSingleton(this);
        About.get().getPackageDetailManager().addWidgetCallback(WidgetProvider.CALLBACK);
        About.get().getPackageDetailManager().init();
    }
}
