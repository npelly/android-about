package org.npelly.android.about.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;

public class TextManager {
    /**
     * Default packages to show.
     */
    private static final List<String> DEFAULT_PACKAGES = Arrays.asList(
            "android",
            "com.google.android.gms",
            "com.google.android.wearable.app",
            "com.google.android.projection.gearhead",
            "com.google.android.apps.maps",
            "com.google.android.apps.gmm",
            "com.google.android.googlequicksearchbox",
            "org.npelly.android.about",
            "org.npelly.android.about.debug"
    );

    public interface Callback {
        void onTextChanged();
    }

    private final Context context;  // Application context.
    private final HashSet<Callback> callbacks = new HashSet<>();
    private final ArrayList<String> customPackages;

    private Spanned widgetText;
    private Spanned activityText;
    private Spanned allText;

    public TextManager(Context context) {
        this.context = context.getApplicationContext();
        customPackages = loadPackages(context);
    }

    public synchronized Spanned getWidgetText() {
        if (widgetText == null) {
            generateText();
        }
        return widgetText;
    }

    public synchronized Spanned getActivityText() {
        if (activityText == null) {
            generateText();
        }
        return activityText;
    }

    public synchronized Spanned getAllText() {
        if (allText == null) {
            generateText();
        }
        return allText;
    }

    public synchronized void addCallback(Callback callback) {
        callbacks.add(callback);
    }

    public synchronized void removeCallback(Callback callback) {
        callbacks.remove(callback);
    }

    private void makeCallbacks() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                About.logd("TextManager making callbacks...");
                for (Callback callback : callbacks) {
                    callback.onTextChanged();
                }
                About.logd("TextManager callbacks complete");
            }
        });
    }

    public synchronized void addPackage(String packageName) {
        About.logd("adding package " + packageName);
        customPackages.add(packageName);
        savePackages(context, customPackages);
    }

    public synchronized void generateText() {
        About.logd("TextManager generateText()");
        long start = SystemClock.elapsedRealtime();

        StringBuilder widgetTextBuilder = new StringBuilder();
        StringBuilder activityTextBuilder = new StringBuilder();
        StringBuilder allTextBuilder = new StringBuilder();

        java.util.Formatter widgetTextFormatter = new java.util.Formatter(widgetTextBuilder);
        java.util.Formatter activityTextFormatter = new java.util.Formatter(activityTextBuilder);
        java.util.Formatter allTextFormatter = new java.util.Formatter(allTextBuilder);

        //TODO cache allPackages
        List<PackageInfo> allPackages = About.get().getContext().getPackageManager().getInstalledPackages(0);

        for (String packageName : DEFAULT_PACKAGES) {
            writeTextForPackage(packageName, false, widgetTextFormatter, activityTextFormatter);
        }
        for (String packageName : customPackages) {
            writeTextForPackage(packageName, true, widgetTextFormatter, activityTextFormatter);
        }
        for (PackageInfo pkg : allPackages) {
            writeTextForPackage(pkg.packageName, true, null, allTextFormatter);
        }

        widgetText = Html.fromHtml(widgetTextBuilder.toString());
        activityText = Html.fromHtml(activityTextBuilder.toString());
        allText = Html.fromHtml(allTextBuilder.toString());

        long delta = SystemClock.elapsedRealtime() - start;
        Log.i(About.TAG, String.format("TextManager generateText() completed in %d ms", delta));

        makeCallbacks();
    }

    private static void writeTextForPackage(String packageName, boolean forcePrint,
                                           Formatter widgetTextFormatter,
                                           Formatter activityTextFormatter) {

        PackageText detail = new PackageText(packageName);

        if (!detail.installed) {
            if (forcePrint) {
                if (detail.name == null) {
                    activityTextFormatter.format("<b>%s</b> not installed<p>", packageName);
                } else {
                    activityTextFormatter.format("<b>%s</b> not installed<p>", detail.name);
                }
            }
            return;
        }

        String certString = "<unknown>";
        if (detail.certificateStrings.length > 0) {
            certString = detail.certificateStrings[0];
        }

        String updateDateTime = DateFormat.getDateTimeInstance().format(
                new Date(detail.packageInfo.lastUpdateTime));

        if (widgetTextFormatter != null) {
            widgetTextFormatter.format(
                    "<b>%s</b>&nbsp&nbsp %s<br>\n",
                    detail.name, detail.longVersionString);
        }
        activityTextFormatter.format(
                "<b>%s</b> %s <br>" +
                        "&nbsp&nbsp&nbsp&nbsp package: %s<br>" +
                        "&nbsp&nbsp&nbsp&nbsp certificate: %s<br>" +
                        "&nbsp&nbsp&nbsp&nbsp last updated: %s<p>",
                detail.name,
                detail.longVersionString,
                detail.packageInfo.packageName,
                certString,
                updateDateTime);
    }

    private static void savePackages(Context context, List<String> packages) {
        About.logd("TextManager savePackages() count=%d", packages.size());

        SharedPreferences sp = context.getSharedPreferences("About", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        StringBuilder sb = new StringBuilder(packages.size() * 20);
        for (int i = 0; i < packages.size(); i++) {
            sb.append(packages.get(i));
            if (i != packages.size() - 1) {
                sb.append(",");
            }
        }
        editor.putString("packages2", sb.toString());
        editor.apply();
    }

    private static ArrayList<String> loadPackages(Context context) {
        About.logd("TextManager loadPackages()");

        SharedPreferences sp = context.getSharedPreferences("About", Activity.MODE_PRIVATE);
        String packageList = sp.getString("packages2", null);
        if (packageList == null) {
            return new ArrayList<>(0);
        }

        String[] packageNames = packageList.split(",");
        About.logd("TextManager loadPackages() loaded %d", packageNames.length);

        return new ArrayList<>(Arrays.asList(packageNames));
    }
}
