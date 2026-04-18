# 🚀 Lorapok Keuboard — Quick Start Guide
## Build Your First Bengali AI Keyboard in 30 Minutes

**Version:** 1.0 | **Last Updated:** April 18, 2026 | **Time Required:** 30-60 minutes

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Android Studio | 2024.1+ | [developer.android.com](https://developer.android.com/studio) |
| JDK | 17+ | Bundled with Android Studio |
| Git | 2.30+ | [git-scm.com](https://git-scm.com) |
| Python | 3.9+ | [python.org](https://python.org) |

**Hardware:** 8GB RAM min, Android phone (5.0+) or Emulator, ~10GB disk

---

## Step 1: Environment Setup

```bash
# Ubuntu/Debian
sudo snap install android-studio --classic

# Set environment variables (~/.bashrc)
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

**Android SDK:** Install Platform 34, Build-Tools 34.0.0, Platform-Tools via SDK Manager.

**Phone:** Settings → About → Tap Build Number 7× → Enable USB Debugging.

---

## Step 2: Create Android Project

1. Android Studio → New Project → Empty Activity
2. **Name:** Lorapok Keuboard
3. **Package:** `com.lorapok.keyboard`
4. **Language:** Kotlin
5. **Min SDK:** API 21 (Android 5.0)

### AndroidManifest.xml — Add Keyboard Service

```xml
<service android:name=".BengaliKeyboardService"
    android:label="Lorapok Keuboard"
    android:permission="android.permission.BIND_INPUT_METHOD"
    android:exported="true">
    <intent-filter>
        <action android:name="android.view.InputMethod" />
    </intent-filter>
    <meta-data android:name="android.view.im" android:resource="@xml/method" />
</service>
```

### res/xml/method.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<input-method xmlns:android="http://schemas.android.com/apk/res/android"
    android:settingsActivity="com.lorapok.keyboard.SettingsActivity"
    android:supportsSwitchingToNextInputMethod="true">
    <subtype android:label="Bengali (বাংলা)" android:imeSubtypeLocale="bn" android:imeSubtypeMode="keyboard" />
    <subtype android:label="English" android:imeSubtypeLocale="en" android:imeSubtypeMode="keyboard" />
</input-method>
```

### Dependencies (build.gradle.kts)

```kotlin
dependencies {
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

## Step 3: Add Core Files

1. Copy `android/android_keyboard_code.kt` → `app/src/main/java/com/lorapok/keyboard/`
2. Copy `data/avro_phonetic_rules.json` → `app/src/main/assets/`
3. Build: `./gradlew assembleDebug`
4. Install: `./gradlew installDebug`

---

## Step 4: Test Phonetic Conversion

Enable keyboard: Settings → Languages & Input → On-screen keyboard → Enable Lorapok Keuboard

| Type This | Expected | Meaning |
|-----------|----------|---------|
| `ami` | আমি | I |
| `tumi` | তুমি | You (informal) |
| `apni` | আপনি | You (formal) |
| `bhalo` | ভালো | Good |
| `bangladesh` | বাংলাদেশ | Bangladesh |
| `dhaka` | ঢাকা | Dhaka |

---

## Troubleshooting

- **Keyboard not in settings:** Rebuild, reinstall, verify AndroidManifest.xml
- **Phonetic not working:** Check `avro_phonetic_rules.json` in assets/, check logcat
- **Crashes:** `adb logcat -s "AndroidRuntime"` — comment out ML parts for basic testing
- **Bengali shows as boxes:** Use Google Play emulator images (not AOSP)

---

## Success Checklist

- [ ] Project builds without errors
- [ ] Keyboard appears in device settings
- [ ] Can type English
- [ ] `ami` → আমি works
- [ ] Suggestions appear
- [ ] Language switch works
- [ ] Can type: আমি ভালো আছি

---

## Next Steps

| Goal | File |
|------|------|
| Add tone rewriting | `android/ToneRewriterImplementation.kt` |
| Train AI model | `scripts/train_bengali_model.py` |
| Collect data | `scripts/collect_bengali_data.py` |
| Design reference | `docs/keyboard_design_specification.md` |
| Research paper | `docs/research_paper_template.md` |

**বাংলা ভাষার জন্য কাজ করুন! (Work for the Bengali language!)**
