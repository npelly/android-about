package org.npelly.android.about.common;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PackageDetail {
    private static final Detailer[] DETAILERS = {
            new SystemDetailer(),
            new GsaDetailer(),
    };

    private static final int PACKAGE_INFO_FLAGS = PackageManager.GET_SIGNATURES;

    public final String readableName;
    public final String packageName;
    public final Spanned verboseSpan;
    public final Spanned terseSpan;
    public final boolean isPinned;
    public final boolean isInstalled;
    public final boolean isSystemSigned;

    // Members below are null if installed is false
    public final PackageInfo packageInfo;
    public final Drawable icon;

    /**
     * Create a new PackageDetail to describe a package, calling PackageManager if necessary.
     * @param packageName required
     * @param packageInfo optional, include if available
     * @param packageManager required
     * @param isPinned if this package is pinned
     * @return new PackageDetail object
     */
    public static PackageDetail newInstance(String packageName, PackageInfo packageInfo,
                                            PackageManager packageManager, boolean isPinned) {
        if (packageInfo == null) {
            try {
                packageInfo = packageManager.getPackageInfo(packageName, PACKAGE_INFO_FLAGS);
            } catch (PackageManager.NameNotFoundException e) { }
        }

        if (packageInfo == null) {
            return newUninstalled(packageName, isPinned);
        } else {
            return newInstalled(packageInfo, packageManager, isPinned);
        }
    }

    public static PackageDetail newUninstalled(String packageName, boolean isPinned) {
        String readableName = null;
        Detailer detailer = getDetailer(packageName);
        if (detailer != null) {
            readableName = detailer.toName(packageName);
        }
        if (readableName == null) {
            readableName = packageName;
        }
        SpannableStringBuilder terseSpan = new SpannableStringBuilder();
        append(terseSpan, readableName, new StyleSpan(Typeface.BOLD), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        terseSpan.append(" not installed\n");
        SpannableStringBuilder verboseSpan = new SpannableStringBuilder(terseSpan);
        verboseSpan.append("\n\n\n");

        return new PackageDetail(readableName, packageName, verboseSpan, terseSpan, isPinned, false, false,
                null, null);
    }

    public static PackageDetail newInstalled(PackageInfo packageInfo, PackageManager packageManager,
                                             boolean isPinned) {
        Detailer detailer = getDetailer(packageInfo.packageName);
        String readableName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        StringBuilder shortVersionString = new StringBuilder();
        StringBuilder longVersionString = new StringBuilder();

        shortVersionString.append(packageInfo.versionName);
        longVersionString.append(packageInfo.versionName).append(" / ");
        longVersionString.append(packageInfo.versionCode);

        if (detailer != null) {
            readableName = detailer.toName(packageInfo.packageName);
            String versionCodename = detailer.toVersionCodename(packageInfo.versionCode);
            shortVersionString.append(' ').append(versionCodename);
            longVersionString.append(" / ").append(versionCodename);
        }
        if (readableName == null) {
            readableName = packageInfo.packageName;
        }

        List<byte[]> sha1Hashes = CertificateUtil.packageInfoToSha1s(packageInfo);
        String certificateString = toCertificateString(packageInfo, sha1Hashes);

        SpannableStringBuilder terseSpan = new SpannableStringBuilder();
        append(terseSpan, readableName, new StyleSpan(Typeface.BOLD),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        terseSpan.append("  ").append(longVersionString).append('\n');

        SpannableStringBuilder verboseSpan = new SpannableStringBuilder();
        append(verboseSpan, readableName, new StyleSpan(Typeface.BOLD),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        verboseSpan.append(' ').append(longVersionString).append('\n');
        verboseSpan.append("    package: ").append(packageInfo.packageName).append('\n');
        verboseSpan.append("    certificate: ").append(certificateString).append('\n');
        if (!About.get().getPackageDetailManager().isSystemUpdateTime(packageInfo.lastUpdateTime)) {
            String updateDateTime =
                    DateFormat.getDateTimeInstance().format(new Date(packageInfo.lastUpdateTime));
            verboseSpan.append("    last update: ").append(updateDateTime);
        } else {
            verboseSpan.append("    last update: <none>");
        }

        Drawable icon = null;
        try {
            icon = packageManager.getApplicationIcon(packageInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {}

        boolean isSystemSigned = About.get().getPackageDetailManager().isSystemSignature(packageInfo);
        return new PackageDetail(readableName, packageInfo.packageName, verboseSpan, terseSpan,
                isPinned, true, isSystemSigned, packageInfo, icon);
    }

    public static PackageDetail newCopy(PackageDetail old, boolean isPinned) {
        return new PackageDetail(old.readableName, old.packageName, old.verboseSpan, old.terseSpan,
                isPinned, old.isInstalled, old.isSystemSigned, old.packageInfo, old.icon);
    }

    private PackageDetail(String readableName, String packageName, Spanned verboseSpan,
                          Spanned terseSpan, boolean isPinned, boolean isInstalled,
                          boolean isSystemSigned, PackageInfo packageInfo, Drawable icon) {
        this.readableName = readableName;
        this.packageName = packageName;
        this.verboseSpan = verboseSpan;
        this.terseSpan = terseSpan;
        this.isPinned = isPinned;
        this.isInstalled = isInstalled;
        this.isSystemSigned = isSystemSigned;
        this.packageInfo = packageInfo;
        this.icon = icon;
    }

    private static Detailer getDetailer(String packageName) {
        for (Detailer detailer : DETAILERS) {
            if (detailer.isDetailer(packageName)) {
                return detailer;
            }
        }
        return null;
    }

    private static String toCertificateString(PackageInfo info, List<byte[]> sha1Hashes) {
        if (sha1Hashes.size() < 1) {
            return "<none>";
        }

        StringBuilder sb = new StringBuilder(12);
        int longHashLength = 8;
        int shortHashLength = 4;
        if (sha1Hashes.size() > 1) {
            longHashLength = 4;
            shortHashLength = 2;
            sb.append("{");
        }


        for (int i = 0; i < sha1Hashes.size(); i++) {
            byte[] sha1 = sha1Hashes.get(i);
            String alias = CertificateUtil.sha1ToAlias(info.signatures[i], sha1);

            if (i > 0) {
                sb.append(", ");
            }
            if (alias == null) {
                sb.append(CertificateUtil.sha1ToString(sha1, longHashLength));
                sb.append("...");
            } else {
                sb.append(CertificateUtil.sha1ToString(sha1, shortHashLength));
                sb.append("...(");
                sb.append(alias);
                sb.append(')');
            }
        }
        if (sha1Hashes.size() > 1) {
            sb.append("}");
        }
        return sb.toString();
    }

    /** Copied from SpannableStringBuilder.java, because its not available until API 21 */
    private static SpannableStringBuilder append(SpannableStringBuilder span, CharSequence text,
                                                 Object what, int flags) {
        int start = span.length();
        span.append(text);
        span.setSpan(what, start, span.length(), flags);
        return span;
    }
}
