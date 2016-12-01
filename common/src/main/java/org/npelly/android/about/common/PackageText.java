package org.npelly.android.about.common;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class PackageText {
    private static final Detailer[] DETAILERS = {
            new SystemDetailer(),
            new GsaDetailer(),
    };

    public boolean installed;
    public String name;
    public String shortVersionString;
    public String longVersionString;
    public String[] certificateStrings;
    public PackageInfo packageInfo;

    public PackageText(String packageName) {
        detail(packageName);
    }

    private void detail(String packageName) {
        try {
            packageInfo = About.get().getContext().getPackageManager().
                    getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            // continue with packageInfo = null and installed = false
        }

        // try detailers
        String versionCodename = null;
        for (Detailer detailer : DETAILERS) {
            if (detailer.isDetailer(packageName)) {
                name = detailer.toName(packageName);
                if (installed) {
                    versionCodename = detailer.toVersionCodename(packageInfo.versionCode);
                }
                break;
            }
        }

        // everything else assumes packageInfo is non-null
        if (!installed) {
            return;
        }

        // fallback to using application label
        if (name == null) {
            PackageManager packageManager = About.get().getContext().getPackageManager();
            name = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        }

        // format version strings
        if (versionCodename == null) {
            shortVersionString = packageInfo.versionName;
            longVersionString = String.format("%s / %d",
                    packageInfo.versionName, packageInfo.versionCode);
        } else {
            shortVersionString = String.format("%s %s",
                    packageInfo.versionName, versionCodename);
            longVersionString = String.format("%s / %d / %s",
                    packageInfo.versionName, packageInfo.versionCode, versionCodename);
        }

        List<byte[]> sha1Hashes = CertificateUtil.packageInfoToSha1s(packageInfo);
        certificateStrings = toCertificateStrings(sha1Hashes);
    }

    private String[] toCertificateStrings(List<byte[]> sha1Hashes) {
        ArrayList<String> strings = new ArrayList<>(sha1Hashes.size());

        for (byte[] sha1 : sha1Hashes) {
            String alias = CertificateUtil.sha1ToAlias(sha1);

            if (alias == null) {
                strings.add(String.format("%s...", CertificateUtil.sha1ToString(sha1, 8)));
            }
            strings.add(String.format("%s... (%s)", CertificateUtil.sha1ToString(sha1, 4), alias));
        }
        return strings.toArray(new String[strings.size()]);
    }
}
