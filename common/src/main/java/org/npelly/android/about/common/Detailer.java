package org.npelly.android.about.common;

public interface Detailer {
    boolean isDetailer(String packageName);
    String toName(String packageName);
    String toVersionCodename(int versionCode);
}
