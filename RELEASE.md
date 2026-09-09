# Reel Narcotics — Production Release & Auto-Update Manual

This document defines the production release architecture, cryptographic signing procedure, automated CI/CD pipeline, and rollback process for the **Reel Narcotics** Android application and website distribution.

---

## 1. System Architecture

```
PRIVATE GITHUB REPOSITORY                     PUBLIC VERCEL BLOB & WEBSITE
(REEL-NARCOTICS-APPLICATION)                  (reel-narcotics.vercel.app)
         |
         | Git Tag (e.g. v2.3.0)
         v
  GitHub Actions CI/CD
         |
         +--> Unit Tests (Gradle)
         +--> Build & Sign Release APK
         +--> Compute SHA-256 Digest
         +--> Generate update.json (Schema v1)
         |
         v
   Vercel Blob Storage ('reel-narcotics-releases')
         |
         +--> Immutable: releases/2.3.0/reel-narcotics-2.3.0.apk
         +--> Immutable: releases/2.3.0/update.json
         +--> Mutable:   latest/update.json (Max-Age: 60s)
                               |
                               +-----------------------------+
                               |                             |
                               v                             v
                      Android Application             Website Download
                    (com.zenith.focus.update)   (reel-narcotics.vercel.app)
                               |
                               v
                       Update Checker &
                      PackageInstaller
```

---

## 2. Versioning Specification

Android update availability is evaluated strictly using integer `versionCode` rather than string `versionName`:

- **`versionCode`** (e.g., `4`): Monotonically increasing integer. An update is available if and only if `remote.versionCode > installed.versionCode`.
- **`versionName`** (e.g., `"2.2.0"`): Semantic version displayed in the UI and Git release tags (`v2.2.0`).

### Manifest Schema (`update.json` v1)

```json
{
  "schemaVersion": 1,
  "packageName": "com.zenith.focus",
  "versionCode": 4,
  "versionName": "2.2.0",
  "minimumSupportedVersionCode": 1,
  "mandatory": false,
  "releaseDate": "2026-09-10T00:00:00Z",
  "apk": {
    "url": "https://reel-narcotics-releases.public.blob.vercel-storage.com/releases/2.2.0/reel-narcotics-2.2.0.apk",
    "sizeBytes": 11786392,
    "sha256": "1d67e55808605eaa43f6858ad615546f76696110ed512da3edc14e9d04e049a0"
  },
  "releaseNotes": [
    "Reel Narcotics Release v2.2.0 (Build 4)",
    "Scandinavian Kinfolk Organic Earth & Linen Minimalist UI",
    "Complete Day & Night Mode with persistent settings",
    "Selective Nuclear Lock with granular feed controls",
    "Extended Focus Timer steppers from 0 to 90 Days",
    "Production auto-update checking with SHA-256 verification"
  ]
}
```

---

## 3. Cryptographic Signing Identity

All production APK updates **must** be signed with the identical release key. Android's native `PackageInstaller` automatically rejects updates (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`) if the cryptographic signature does not match the installed package.

- **Keystore File**: `zenith-release.keystore`
- **Key Alias**: `zenith`
- **Certificate SHA-256 Fingerprint**:
  `9D:C4:F5:DE:70:DE:BC:C1:E0:E8:97:DD:52:13:61:55:36:88:AF:42:7D:A7:95:3B:F0:56:68:34:A3:FC:DD:12`

---

## 4. Required GitHub Secrets

The release workflow (`.github/workflows/android-release.yml`) requires the following repository secrets configured under **Settings → Secrets and variables → Actions**:

| Secret Name | Description | Example / Format |
| :--- | :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded `zenith-release.keystore` | `cat zenith-release.keystore \| base64 -w 0` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password | Confidential string |
| `ANDROID_KEY_ALIAS` | Key alias in keystore | `zenith` |
| `ANDROID_KEY_PASSWORD` | Key password | Confidential string |
| `BLOB_READ_WRITE_TOKEN` | Vercel Blob store write token | `vercel_blob_rw_...` (from Vercel Dashboard) |

---

## 5. Release Workflow (Step-by-Step)

### Triggering a Production Release

1. **Increment Version** in `app/build.gradle`:
   - Increment `versionCode` (e.g. `4` ➔ `5`)
   - Update `versionName` (e.g. `"2.2.0"` ➔ `"2.3.0"`)
2. **Commit & Tag**:
   ```bash
   git commit -am "chore(release): bump version to 2.3.0"
   git tag v2.3.0
   git push origin main --tags
   ```
3. **GitHub Actions Automation**:
   - Compiles and runs all unit tests (`testReleaseUnitTest`).
   - Restores the keystore and builds `app-release.apk`.
   - Generates `update.json` with computed SHA-256 digest and byte size.
   - Uploads immutable release assets to Vercel Blob:
     - `releases/2.3.0/reel-narcotics-2.3.0.apk`
     - `releases/2.3.0/update.json`
   - Updates mutable pointer `latest/update.json` (`max-age=60s`).
   - Publishes a formal GitHub Release with release notes.

---

## 6. Rollback Procedure

Because release paths in Vercel Blob are immutable and versioned (`releases/2.2.0/`, `releases/2.1.0/`), rolling back does not require deleting files or rewriting history:

1. Retrieve the known-good manifest (e.g. `releases/2.2.0/update.json`).
2. Overwrite `latest/update.json` with the previous version's manifest:
   ```bash
   npx @vercel/blob put latest/update.json --token=$BLOB_READ_WRITE_TOKEN < releases/2.2.0/update.json
   ```
3. Clients will automatically receive the rolled-back version upon next update check.

---

## 7. Nuclear Mode Safety During Updates

Reel Narcotics enforces unbreakable focus locks. Application updates are guaranteed safe:
1. **Absolute Epoch Timestamps**: Nuclear mode uses `startTimeMillis` and `endTimeMillis`. The state survives process death, OS updates, and APK replacements.
2. **DataStore Preservation**: Application data in `/data/data/com.zenith.focus/` is preserved by Android OS across package updates (`ACTION_MY_PACKAGE_REPLACED`).
3. **Re-arming Alarm on Update**: `BootCompletedReceiver` listens to `Intent.ACTION_MY_PACKAGE_REPLACED` and immediately reschedules exact alarms for active Nuclear sessions upon update completion.

---

## 8. Security Guardrails — WHAT MUST NEVER BE COMMITTED

The following items are strictly gitignored and must **NEVER** be committed to Git:

- Keystores or certificates (`*.keystore`, `*.jks`, `*.p12`)
- Plaintext passwords or secrets
- Vercel tokens (`BLOB_READ_WRITE_TOKEN`, `VERCEL_TOKEN`)
- Environment variable files (`.env`, `.env.local`, `.env.*`)
- Temporary signing artifacts (`ci/temp/`)
