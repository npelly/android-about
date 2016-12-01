package org.npelly.android.about.common;

public class GsaDetailer implements Detailer {
    @Override
    public boolean isDetailer(String packageName) {
        return "com.google.android.googlequicksearchbox".equals(packageName);
    }

    @Override
    public String toName(String packageName) {
        return "GSA";
    }

    @Override
    public String toVersionCodename(int versionCode) {
        return null;
    }
}
