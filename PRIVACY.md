# Zenith Focus Privacy Model ???

Zenith Focus was engineered from day one under a strict, non-negotiable principle: **Your personal data and screen content must never leave your physical device.**

---

## 1. Zero Internet Permission (OS-Level Enforcement)

In AndroidManifest.xml, Zenith Focus **does NOT declare** ndroid.permission.INTERNET.

Because the Android operating system enforces permissions at the kernel and framework layer, an app without ndroid.permission.INTERNET is **cryptographically and physically blocked from making any network requests**:
- No HTTP/HTTPS socket connections
- No background DNS lookups
- No telemetry pings
- No analytics uploads
- No crash-reporting packets

Even if malicious code were somehow introduced into a dependency, Android's SELinux policy prevents network packets from ever reaching the modem or Wi-Fi interface.

---

## 2. Accessibility Privacy Model

Zenith Focus requires Android Accessibility features exclusively to evaluate the visible structure of third-party apps:

### What Zenith Focus DOES Inspect:
- Foreground package name (e.g. com.google.android.youtube)
- View layout IDs (e.g. eel_recycler, clips_video_container)
- Public action button labels (e.g. Remix, Use audio, Shorts)
- Browser address-bar URLs to match against locally stored adult domain rules

### What Zenith Focus NEVER Captures or Stores:
- **No private messages or chats**
- **No keystroke logging**
- **No user passwords or PINs**
- **No screenshots or screen recordings**
- **No contacts, accounts, or emails**
- **No location data or sensor inputs**

---

## 3. Cryptographic Storage of Focus PIN

When a user configures a focus unlock PIN:
- The raw PIN is **never** saved to disk.
- Zenith Focus generates a 16-byte cryptographically secure random salt using java.security.SecureRandom.
- The PIN is hashed using PBKDF2WithHmacSHA256 across 10,000 iterations.
- Verification uses constant-time comparison (slowEquals) to prevent side-channel timing attacks.

---

## 4. Local Data Portability & Deletion

All block statistics are stored locally in SQLite (zenith_focus.db). Users have 100% control:
- **Export Data**: One-tap CSV export of all local block events directly to clipboard.
- **Wipe Everything**: One-tap "Clear History" permanently deletes all recorded block records from SQLite.
