# Android About

*Quick, detailed info about APK's installed.*

### Install

Available on Play Store:

https://play.google.com/store/apps/details?id=org.npelly.android.about

### About Android About

Am I running debug or release APK? Which version is it again?

Android About is an Android utility app for developers & testers who juggle multiple APK variants.
It quickly shows the version name, version code, and certificate hash / signature for installed APKs
and for the base system image.

Android About runs on Phone/Tablet (including a widget), Wear and TV.

Q. Why not use Android->Settings?
A. Android Settings isn't great for disambiguation of different certificate
versions etc, and isn't as quick.

### SOURCE

Source hosted at
https://github.com/npelly/android-about

```
common/
    Shared library with most of the logic.
mobile/
    Phone/Tablet UI
wear/
    Wear UI
```

Optionally add aliases for well known certificate hashes into CertificateUtil.java.

Consider adding Detailer subclasses to provide more detail for APKs, for
example to convert version names and version codes into developer/tester
friendly codenames.

### BUILD
#### DEBUG

```
./gradlew assembleDebug

adb install -r mobile/build/outputs/apk/mobile-debug.apk   # phone/tablet/tv
# or
adb install -r wear/build/outputs/apk/wear-debug.apk       # wear
```

Or, load & build as an Android Studio project.

#### RELEASE

```
./gradlew assembleRelease
PATH=$PATH:[BUILD TOOLS]   # example: ~/Library/Android/sdk/build-tools/25.0.1/
zipalign -f -p 4 mobile/build/outputs/apk/mobile-release-unsigned.apk mobile/build/outputs/apk/mobile-release.apk
apksigner sign --ks [path-to]/org.npelly.android.about.keystore.jks mobile/build/outputs/apk/mobile-release.apk

adb install -r mobile/build/outputs/apk/mobile-release.apk
```

### PUBLISH

````
cp mobile/build/outputs/apk/mobile-release.apk releases/about-[VERSION].apk
# git commit -a, git push, copy to play store, etc
```

### CHANGELOG

v2
    Initial open-source version.
    Supports Phone/Tablet, Wear, TV

v2.0.1
    Minor fixes

v2.0.2
	Fixes crash: exception when certificate key type is OpenSSLDSAPublicKey
	or BCDSAPublicKey.