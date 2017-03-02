# Android About

*Quick access to meta-data of installed Android packages.*


Am I running debug or release APK? Which version do I have installed?

Android About is an Android utility app for developers & testers who juggle multiple APK variants.
It quickly shows the version name, version code, and certificate hash / signature for installed APKs
and for the base system image.

Android About runs on Phone/Tablet (including a widget), Wear and TV.

Q. Why not use Android->Settings?
A. Android Settings isn't great for disambiguation of different certificate
versions etc, and isn't as quick.


### Install

Available on Play Store:

https://play.google.com/store/apps/details?id=org.npelly.android.about


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
# VERSION in format vX.Y, for example "v2.3"
git tag VERSION
git push origin VERSION
```

Gradle scripts generate version code and version name from this git tag, and
http://www.buddybuild.com will auto-generate a signed release build on new git tags.

### CHANGELOG

```
v2.3
    Initial introdution of view pager to switch between pinned package & all packages.

v2.2
    Fixes crash: exception when certificate key type is OpenSSLDSAPublicKey or BCDSAPublicKey.

v2.1
    Minor fixes.

v2.0
    Initial open-source version.
    Supports Phone/Tablet, Wear, TV
```