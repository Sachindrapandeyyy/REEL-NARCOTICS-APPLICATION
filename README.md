# Reel Narcotics ⚡
> Production-grade, offline-first Android digital-wellbeing and dopamine-detox application.
> Surgically intercepts addictive short-form feeds (YouTube Shorts, Instagram Reels, Snapchat Spotlight, Facebook Reels, TikTok) and Adult/Explicit content with absolute-timestamp focus locks, tamper resistance, device-admin deactivation prevention, anti-impulse friction, and an interactive mascot.

---

## 🚀 Product Overview

Modern social platforms intentionally deploy variable reward loops and infinite vertical swipe mechanics to capture dopamine and attention. **Reel Narcotics** surgically intercepts the addictive algorithmic video feeds without locking you out of essential communications:

- **YouTube**: Blocks Shorts viewer, shelves, and tabs while preserving full access to long-form educational videos, tutorials, searches, channels, and playlists.
- **Instagram**: Blocks Reels viewer, audio pages, and bottom navigation tabs while preserving direct messages (DMs), user profiles, and feed photos.
- **Snapchat**: Blocks Spotlight vertical video feeds and infinite stories while keeping chats and camera capture accessible.
- **Facebook**: Blocks Facebook Reels, while keeping groups, marketplace, and standard feeds available.
- **TikTok**: Complete application lock during active focus sessions.
- **Adult & Explicit Content Shield**: Multi-layered local domain blocklist (500+ domains), real-time browser address-bar analysis (Chrome, Firefox, Edge, Brave, Opera, Samsung Internet), and token-aware explicit text filtering.
- **Tamper & Delete Protection**: Hardened Device Admin integration that detects and blocks unauthorized app uninstalls or attempts to deactivate administrative permissions during active Focus Locks or Nuclear Mode.
- **Absolute-Timestamp Focus Lock**: Survives process death, battery saver kills, orientation changes, and device reboots. Never counts down in volatile memory.
- **Anti-Impulse Friction Layer**: Requires intentional effort (5-second hold, mindful affirmation typing, mental arithmetic, or security PIN) to prevent impulsive unlocking.
- **Original Mascot**: Mascot rendered via zero-dependency Jetpack Compose Canvas across 7 expressive states (HEALTHY, FOCUSED, ALERT, PROTECTED, BLOCKING, STREAK, RECOVERY).

---

## 🔒 100% Offline • Zero Telemetry Guarantee

- **No Internet Permission**: The app does **NOT** declare `android.permission.INTERNET` in its `AndroidManifest.xml`. It cannot make network requests even if compromised.
- **Zero Cloud Services**: No Firebase, no AWS, no Supabase, no third-party analytics, trackers, or advertising SDKs.
- **Local Relational Database**: All block events, streak tracking, and statistics are stored locally in SQLite (`zenith_focus.db`).

---

## 📱 Supported Android Versions

- **Minimum SDK**: Android 7.0 (API 24 - Nougat)
- **Target SDK**: Android 13.0 (API 33 - Tiramisu)
- **Compile SDK**: Android 14.0 (API 34 - UpsideDownCake)
- **Tested Architectures**: `arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`

---

## 🛠️ Build & Installation

### Prerequisites
- JDK 17
- Android SDK Platform 33 / 34
- Android Build Tools 33.0.1+

### Build Release APK
```bash
./gradlew assembleRelease --no-daemon
```
The output APK is generated at:
`app/build/outputs/apk/release/app-release.apk`

### Install to Android Device via ADB
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 🛡️ Initial Setup & Permissions

1. **Launch Reel Narcotics**: Open the app and follow the onboarding flow.
2. **Enable Accessibility Service**:
   - Navigate to Android **Settings** > **Accessibility** > **Downloaded Apps** / **Installed Services**.
   - Tap **Reel Narcotics Shield** and toggle **ON**.
3. **Grant Floating Overlay Permission**:
   - Allows displaying the smooth floating block screen directly over short-form feeds (`TYPE_APPLICATION_OVERLAY`).
4. **Enable Delete & Tamper Protection (Optional)**:
   - Activates Device Admin privileges to prevent premature app uninstallation or unauthorized service disabling during active focus sessions.
5. **Choose Protected Platforms**:
   - In the **Shield** tab, select YouTube Shorts, Instagram Reels, Snapchat Spotlight, Adult Websites, etc.
6. **Activate a Focus Lock**:
   - Choose a preset (15m, 1h, 6h, 1 day, 3 days, 7 days) or set a custom duration / "Until Tomorrow".

---

## 📚 Technical Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - Clean architecture, data flow, reactive state machine, and persistence layer.
- [DETECTION_ENGINE.md](DETECTION_ENGINE.md) - Multi-signal heuristics, node parsing, confidence weights, and rule versioning.
- [PRIVACY.md](PRIVACY.md) - Cryptographic verification of zero network access and offline safety guarantees.
- [TESTING.md](TESTING.md) - Unit test suite, detector test matrix, and verification instructions.

---

## ⚖️ License & Disclaimer

Reel Narcotics is designed for personal digital wellbeing and discipline enhancement. All platform names and trademarks belong to their respective owners.
