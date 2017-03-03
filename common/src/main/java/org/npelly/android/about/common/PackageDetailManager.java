package org.npelly.android.about.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages
 *   list of pinned pacakges
 *   list of all packages
 *   list of package details
 */
public class PackageDetailManager {
    public interface WidgetCallback {
        void onChange(Spanned widgetSpan);
    }

    public interface PackageChangeCallback {
        void onPackageChanged(PackageDetail detail);
        void onInitComplete(Collection<PackageDetail> initDetails);
    }

    public static final Comparator<PackageDetail> DEFAULT_SORT_ORDER = new Comparator<PackageDetail>() {
        @Override
        public int compare(PackageDetail detail1, PackageDetail detail2) {
            int result = detail1.readableName.compareToIgnoreCase(detail2.readableName);
            if (result == 0) {
                result = detail1.packageName.compareTo(detail2.packageName);
            }
            return result;
        }
    };

    private final PackageManager packageManager;

    private final HashMap<String, PackageDetail> packageDetails = new HashMap<>();
    private final HashSet<WidgetCallback> widgetCallbacks = new HashSet<>();
    private final HashSet<PackageChangeCallback> packageCallbacks = new HashSet<>();

    private boolean isInitComplete;
    private Signature[] systemSignatures;
    private long systemUpdateTime;
    private Spanned widgetSpan = new SpannedString("loading...");

    PackageDetailManager(Context context) {
        packageManager = context.getPackageManager();
    }

    public boolean isInitComplete() {
        return isInitComplete;
    }

    public Collection<PackageDetail> getPackageDetails() {
        return packageDetails.values();
    }

    public Collection<PackageDetail> addPackageChangeCallback(PackageChangeCallback callback) {
        packageCallbacks.add(callback);
        return packageDetails.values();
    }

    public void removePackageChangeCallback(PackageChangeCallback callback) {
        packageCallbacks.remove(callback);
    }

    public Spanned getWidgetSpan() {
        return widgetSpan;
    }

    public void addWidgetCallback(WidgetCallback callback) {
        widgetCallbacks.add(callback);
    }

    public void removeWidgetCallback(WidgetCallback callback) {
        widgetCallbacks.remove(callback);
    }

    private void updateWidgetSpanAndNotify() {
        About.assertMainThread();

        SpannableStringBuilder span = new SpannableStringBuilder();
        ArrayList<PackageDetail> details = new ArrayList<>();
        for (PackageDetail detail : packageDetails.values()) {
            if (detail.isPinned) {
                details.add(detail);
            }
        }
        Collections.sort(details, DEFAULT_SORT_ORDER);
        for (PackageDetail detail : details) {
            span.append(detail.terseSpan);
        }
        this.widgetSpan = span;

        for (WidgetCallback callback : widgetCallbacks) {
            callback.onChange(widgetSpan);
        }
    }

    public void pin(PackageDetail detail) {
        About.logd("PackageDetailManager pin(%s)", detail.packageName);

        detail = PackageDetail.newCopy(detail, true);
        packageDetails.put(detail.packageName, detail);
        for (PackageChangeCallback callback : packageCallbacks) {
            callback.onPackageChanged(detail);
        }

        updateWidgetSpanAndNotify();

        About.get().getPrefManager().addPinnedPackage(detail.packageName);
    }

    public void unpin(PackageDetail detail) {
        About.logd("PackageDetailManager unpin(%s)", detail.packageName);

        detail = PackageDetail.newCopy(detail, false);
        packageDetails.put(detail.packageName, detail);
        for (PackageChangeCallback callback : packageCallbacks) {
            callback.onPackageChanged(detail);
        }

        updateWidgetSpanAndNotify();

        About.get().getPrefManager().removePinnedPackage(detail.packageName);
    }

    public void changePackage(String packageName) {
        About.logd("PackageDetailManager changePackage(%s)", packageName);

        boolean isPinned = About.get().getPrefManager().getPinnedPackages().contains(packageName);
        PackageDetail detail = PackageDetail.newInstance(packageName, null, packageManager, isPinned);
        packageDetails.put(detail.packageName, detail);
        for (PackageChangeCallback callback : packageCallbacks) {
            callback.onPackageChanged(detail);  // let adapter decide if its really removed
        }

        if (isPinned) {
            updateWidgetSpanAndNotify();
        }
    }

    public void init() {
        new Thread() {
            @Override
            public void run() {
                initBackground();
            }
        }.start();
    }

    private void initBackground() {
        About.logd("PackageDetailManager initBackground()");
        long start = SystemClock.elapsedRealtime();

        HashMap<String, PackageDetail> details = new HashMap<>();
        HashSet<String> pinnedPackages =
                new HashSet<>(About.get().getPrefManager().getPinnedPackages());

        // init system package info, needed to help detail the other packages
        initSystemPackage();

        // 1) Get all installed packages
        List<PackageInfo> allPackages =
                packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);
        for (PackageInfo packageInfo : allPackages) {
            boolean isPinned = pinnedPackages.remove(packageInfo.packageName);
            PackageDetail detail = PackageDetail.newInstalled(packageInfo, packageManager, isPinned);
            details.put(detail.packageName, detail);
        }

        // 2) Get any remaining (uninstalled) pinned pacakges
        for (String packageName : pinnedPackages) {
            PackageDetail detail = PackageDetail.newInstance(packageName, null, packageManager, true);
            details.put(detail.packageName, detail);
        }

        long delta = SystemClock.elapsedRealtime() - start;
        About.logi("PackageDetailManager initBackground() completed in %d ms", delta);

        initCompleteToMainThread(details);
    }

    private void initCompleteToMainThread(final HashMap<String, PackageDetail> details) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                packageDetails.putAll(details);
                isInitComplete = true;
                for (PackageChangeCallback callback : packageCallbacks) {
                    callback.onInitComplete(packageDetails.values());
                }
                updateWidgetSpanAndNotify();
            }
        });
    }

    private void initSystemPackage() {
        try {
            PackageInfo info = packageManager.getPackageInfo("android", packageManager.GET_SIGNATURES);
            if (info == null || info.signatures == null || info.signatures.length < 1) {
                About.logw("Failed to init system signature: android package not signed");
                return;
            } else if (info.signatures.length > 1) {
                About.logi("Multiple system signatures, using greedy matching (match any)");
            }
            systemUpdateTime = info.lastUpdateTime;
            systemSignatures = info.signatures;
        } catch (PackageManager.NameNotFoundException e) {
            About.logw("Failed to init system signature: android package not found");
        }
    }

    public boolean isSystemSignature(Signature sig1) {
        if (systemSignatures == null) return false;
        for (Signature sig2 : systemSignatures) {
            if (sig1 != null && sig1.equals(sig2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSystemUpdateTime(long updateTime) {
        return updateTime == systemUpdateTime;
    }

    public boolean isSystemSignature(PackageInfo info) {
        if (systemSignatures == null) {
            return false;
        }
        if (info == null || info.signatures == null) {
            return false;
        }
        // greedy match any combination
        for (Signature sig1 : info.signatures) {
            if (isSystemSignature(sig1)) {
                return true;
            }
        }
        return false;
    }
}
