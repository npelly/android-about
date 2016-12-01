package org.npelly.android.about.common;

import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SystemDetailer implements Detailer {
    @Override
    public boolean isDetailer(String packageName) {
        return "android".equals(packageName);
    }

    @Override
    public String toName(String packageName) {
        return "Android System";
    }

    @Override
    public String toVersionCodename(int versionCode) {
        Field[] fields = Build.VERSION_CODES.class.getDeclaredFields();

        for (Field f : fields) {
            try {
                int m = f.getModifiers();
                if (Modifier.isStatic(m) && Modifier.isFinal(m) && Modifier.isPublic(m)) {
                    if (f.getInt(f) == Build.VERSION.SDK_INT) {
                        return f.getName();
                    }
                }
            } catch (IllegalAccessException e) {
                // continue
            }
        }
        return null;
    }
}
