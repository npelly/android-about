package org.npelly.android.about.common;


import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CertificateUtil {
    /**
     * Convenience aliases for some popular certificate SHA1's.
     */
    private static final List<Pair<byte[], String>> SHA1_ALIASES = createSha1AliasList(new String[]{
            "Android release-keys", "38:91:8A:45:3D:07:19:93:54:F8:B1:9A:F0:5E:C6:56:2C:ED:57:88",
            "Clockwork release-keys", "A1:97:F9:21:2F:2F:ED:64:F0:FF:9C:2A:4E:DF:24:B9:C8:80:1C:8C",
            "Samsung release-keys", "9C:A5:17:0F:38:19:19:DF:E0:44:6F:CD:AB:18:B1:9A:14:3B:31:63",
            "Google release-keys", "24:BB:24:C0:5E:47:E0:AE:FA:68:A5:8A:76:61:79:D9:B6:13:A6:00",
            "About release-keys", "E5:5D:72:17:28:DC:B2:52:48:68:E6:9D:B6:57:17:0F:4E:BA:F7:D1",
    });

    private static ArrayList<Pair<byte[], String>> createSha1AliasList(String[] rawList) {
        ArrayList<Pair<byte[], String>> list = new ArrayList<>(rawList.length / 2);
        for (int i = 0; i < rawList.length; i += 2) {
            String rawSha1 = rawList[i + 1];
            String alias = rawList[i];

            list.add(new Pair<>(sha1StringToByteArray(rawSha1), alias));
        }
        return list;
    }

    private static byte[] sha1StringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len + 1) / 3];
        for (int i = 0; i < len; i += 3) {
            data[i / 3] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String sha1ToAlias(byte[] sha1) {
        for (Pair<byte[], String> sha1Alias : SHA1_ALIASES) {
            if (Arrays.equals(sha1Alias.first, sha1)) {
                return sha1Alias.second;
            }
        }
        return null;
    }

    public static String sha1ToString(byte[] sha1, int length) {
        StringBuilder b = new StringBuilder(length * 3);
        b.append(String.format("%02X", sha1[0]));
        for (int i = 1; i < sha1.length && i < length; i++) {
            b.append(String.format(":%02X", sha1[i]));
        }
        return b.toString();
    }

    public static List<byte[]> packageInfoToSha1s(PackageInfo packageInfo) {
        ArrayList<byte[]> hashes = new ArrayList<>(packageInfo.signatures.length);

        for (Signature signature : packageInfo.signatures) {
            try {

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                        new ByteArrayInputStream(signature.toByteArray()));
                PublicKey key = certificate.getPublicKey();

                if (!(key instanceof RSAPublicKey)) {
                    // TODO: figure out how to handle OpenSSLDSAPublicKey
                    Log.w(About.TAG, String.format("%s has unknown key class %s",
                            packageInfo.packageName, key.getClass().getSimpleName()));
                    continue;
                }

                MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
                byte[] sha1 = messageDigest.digest(certificate.getEncoded());

                if (sha1ToAlias(sha1) == null) {
                    Log.i(About.TAG, String.format("no sha1 alias for %s %s",
                            sha1ToString(sha1, sha1.length),
                            certificate.getSubjectDN().toString()));
                    // Log.i(About.TAG, certificate.toString());
                }
                hashes.add(sha1);
            } catch (CertificateException | NoSuchAlgorithmException e) {
                Log.w(About.TAG, packageInfo.packageName, e);
            }
        }

        return hashes;
    }

}
