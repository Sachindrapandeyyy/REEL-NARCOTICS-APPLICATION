#!/usr/bin/env node
import fs from 'fs';
import path from 'path';
import crypto from 'crypto';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const projectRoot = path.resolve(__dirname, '..');

// 1. Read build.gradle to parse versionName and versionCode dynamically
function getAppVersionInfo() {
  const gradlePath = path.join(projectRoot, 'app', 'build.gradle');
  if (!fs.existsSync(gradlePath)) {
    throw new Error(`Gradle file not found at: ${gradlePath}`);
  }
  const content = fs.readFileSync(gradlePath, 'utf8');

  const vCodeMatch = content.match(/versionCode\s+(\d+)/);
  const vNameMatch = content.match(/versionName\s+"([^"]+)"/);
  const appIdMatch = content.match(/applicationId\s+"([^"]+)"/);

  if (!vCodeMatch || !vNameMatch) {
    throw new Error('Unable to extract versionCode or versionName from app/build.gradle');
  }

  return {
    versionCode: parseInt(vCodeMatch[1], 10),
    versionName: vNameMatch[1],
    packageName: appIdMatch ? appIdMatch[1] : 'com.zenith.focus'
  };
}

// 2. Compute SHA-256 hex digest
function computeSha256(filePath) {
  const fileBuffer = fs.readFileSync(filePath);
  const hashSum = crypto.createHash('sha256');
  hashSum.update(fileBuffer);
  return hashSum.digest('hex').toLowerCase();
}

async function main() {
  const isDryRun = process.argv.includes('--dry-run');
  const isRelease = process.argv.includes('--release') || !isDryRun;

  console.log('🚀 Reel Narcotics Release & Update Packager');
  console.log('==========================================');

  const versionInfo = getAppVersionInfo();
  console.log(`Package Name: ${versionInfo.packageName}`);
  console.log(`Version Name: ${versionInfo.versionName}`);
  console.log(`Version Code: ${versionInfo.versionCode}`);

  const candidatePaths = [
    path.join(projectRoot, 'app', 'build', 'outputs', 'apk', 'githubDirect', 'release', 'app-githubDirect-release.apk'),
    path.join(projectRoot, 'app', 'build', 'outputs', 'apk', 'release', 'app-release.apk'),
    path.join(projectRoot, 'app', 'build', 'outputs', 'apk', 'githubDirect', 'release', 'app-githubDirect-release-unsigned.apk'),
    path.join(projectRoot, 'app', 'build', 'outputs', 'apk', 'release', 'app-release-unsigned.apk')
  ];
  const defaultApkPath = candidatePaths.find(p => fs.existsSync(p));
  if (!defaultApkPath) {
    throw new Error(`Release APK not found. Looked in: ${candidatePaths.join(', ')}. Run './gradlew assembleGithubDirectRelease' or './gradlew assembleRelease' first.`);
  }

  const apkStats = fs.statSync(defaultApkPath);
  const apkSizeBytes = apkStats.size;
  const apkSha256 = computeSha256(defaultApkPath);

  console.log(`APK Path:     ${defaultApkPath}`);
  console.log(`APK Size:     ${(apkSizeBytes / (1024 * 1024)).toFixed(2)} MB (${apkSizeBytes} bytes)`);
  console.log(`SHA-256:      ${apkSha256}`);

  // Create release staging directory
  const distDir = path.join(projectRoot, 'build', 'release-dist');
  if (!fs.existsSync(distDir)) {
    fs.mkdirSync(distDir, { recursive: true });
  }

  const versionedApkName = `reel-narcotics-${versionInfo.versionName}.apk`;
  const stagedApkPath = path.join(distDir, versionedApkName);
  fs.copyFileSync(defaultApkPath, stagedApkPath);

  // Construct target public Blob storage URL
  const publicBlobBaseUrl = process.env.PUBLIC_BLOB_BASE_URL ||
    `https://wrsps1etaszror9m.public.blob.vercel-storage.com`;
  const remoteApkUrl = `${publicBlobBaseUrl}/releases/${versionInfo.versionName}/${versionedApkName}`;

  // Read release notes from Git log or environment if available
  const releaseNotes = [
    `Reel Narcotics Release v${versionInfo.versionName} (Build ${versionInfo.versionCode})`,
    "Free DIY Physical Blocker: NFC Card tap / QR Code focus key (with 15m anti-impulse cooldown override)",
    "Auto-Grayscale Monochrome Mode: sensory dopamine kill during Nuclear Mode via Android Daltonizer",
    "Daily Focus Score (0–100) & Time Reclaimed: Opal-style positive reinforcement powered 100% offline",
    "5-Second Mindful Breathing Pause: One Sec model breaking autopilot muscle-memory app opening",
    "Intent & Time-Box Picker: Regain / ScreenZen stopping cues with subtle top progress indicator",
    "Gentle Doomscroll Warning Intercept: velocity guard detecting infinite feed dopamine loops",
    "Universal Anti-Tamper Shield: blocks OEM freezing, deep sleeping, date & time manipulation during Nuclear locks",
    "Physical Bottom-Tab Click Shield: intercepts Shorts, Reels, and Spotlight navigation tabs with stealth styling",
    "Universal Short-Form App Blocker: surgical ejection for TikTok, Moj, Josh, ShareChat, Chingari, Hipi, Roposo, MX TakaTak",
    "Browser Short-Form URL Shield: blocks /shorts, /reels, and tiktok.com URLs on all browsers",
    "Reboot & Clock-Jump Hardening: monotonic clock re-anchoring across reboots",
    "Optional ADB God Mode (Device Owner): prevents uninstallation and settings bypass",
    "Dual Distribution Flavors: Vercel/GitHub Direct OTA updates and Play Store compliance"
  ];

  const updateManifest = {
    schemaVersion: 1,
    packageName: versionInfo.packageName,
    versionCode: versionInfo.versionCode,
    versionName: versionInfo.versionName,
    minimumSupportedVersionCode: 1,
    mandatory: false,
    releaseDate: new Date().toISOString(),
    apk: {
      url: remoteApkUrl,
      sizeBytes: apkSizeBytes,
      sha256: apkSha256
    },
    releaseNotes: releaseNotes
  };

  const stagedUpdateJsonPath = path.join(distDir, 'update.json');
  fs.writeFileSync(stagedUpdateJsonPath, JSON.stringify(updateManifest, null, 2), 'utf8');
  console.log(`✅ Staged update.json generated at: ${stagedUpdateJsonPath}`);

  // 3. Vercel Blob Upload Authentication
  let blobToken = process.env.BLOB_READ_WRITE_TOKEN?.trim();
  if (!blobToken) {
    const envFile = path.join(projectRoot, '.env.vercel.local');
    if (fs.existsSync(envFile)) {
      const match = fs.readFileSync(envFile, 'utf8').match(/BLOB_READ_WRITE_TOKEN="?([^"\n\r]+)"?/);
      if (match) {
        blobToken = match[1].trim();
      }
    }
  }

  const repoName = process.env.GITHUB_REPOSITORY || 'Sachindrapandeyyy/REEL-NARCOTICS-APPLICATION';
  const githubReleaseApkUrl = `https://github.com/${repoName}/releases/download/v${versionInfo.versionName}/${versionedApkName}`;

  if (isDryRun) {
    console.log('\nℹ️ Dry run mode active (--dry-run). Skipping Vercel Blob upload.');
    console.log(`Release artifacts staged locally in: ${distDir}`);
  } else if (!blobToken) {
    console.log('\n⚠️ BLOB_READ_WRITE_TOKEN is not configured or empty.');
    console.log('ℹ️ Skipping Vercel Blob upload. Release artifacts will be distributed via GitHub Releases.');
    updateManifest.apk.url = githubReleaseApkUrl;
    fs.writeFileSync(stagedUpdateJsonPath, JSON.stringify(updateManifest, null, 2), 'utf8');
  } else {
    console.log('\n📦 Uploading release artifacts to Vercel Blob store...');
    try {
      const { put } = await import('@vercel/blob');

      // Upload versioned APK
      console.log(`Uploading releases/${versionInfo.versionName}/${versionedApkName}...`);
      const apkStream = fs.createReadStream(stagedApkPath);
      const apkBlob = await put(`releases/${versionInfo.versionName}/${versionedApkName}`, apkStream, {
        access: 'public',
        token: blobToken,
        addRandomSuffix: false
      });
      console.log(`Uploaded APK: ${apkBlob.url}`);

      // Update URL in manifest if different
      updateManifest.apk.url = apkBlob.url;
      fs.writeFileSync(stagedUpdateJsonPath, JSON.stringify(updateManifest, null, 2), 'utf8');

      // Upload versioned update.json
      console.log(`Uploading releases/${versionInfo.versionName}/update.json...`);
      const versionedJsonBlob = await put(`releases/${versionInfo.versionName}/update.json`, JSON.stringify(updateManifest, null, 2), {
        access: 'public',
        token: blobToken,
        addRandomSuffix: false,
        contentType: 'application/json'
      });
      console.log(`Uploaded Versioned Manifest: ${versionedJsonBlob.url}`);

      // Upload latest/update.json (mutable pointer)
      console.log('Updating latest/update.json...');
      const latestJsonBlob = await put('latest/update.json', JSON.stringify(updateManifest, null, 2), {
        access: 'public',
        token: blobToken,
        addRandomSuffix: false,
        contentType: 'application/json',
        cacheControlMaxAge: 60
      });
      console.log(`Updated latest manifest: ${latestJsonBlob.url}`);
      console.log('\n🎉 Vercel Blob release deployment complete!');

    } catch (err) {
      console.warn('\n⚠️ Warning: Vercel Blob upload encountered an error:', err.message);
      console.warn('ℹ️ Falling back to GitHub Releases for APK distribution.');
      updateManifest.apk.url = githubReleaseApkUrl;
      fs.writeFileSync(stagedUpdateJsonPath, JSON.stringify(updateManifest, null, 2), 'utf8');
    }
  }

  // 4. Output GitHub Actions step parameters if running in GitHub Actions
  const githubOutput = process.env.GITHUB_OUTPUT;
  if (githubOutput) {
    const outputs = [
      `versionName=${versionInfo.versionName}`,
      `versionCode=${versionInfo.versionCode}`,
      `apkSha256=${apkSha256}`,
      `apkSizeBytes=${apkSizeBytes}`,
      `apkPath=${stagedApkPath}`,
      `updateJsonPath=${stagedUpdateJsonPath}`,
      `apkName=${versionedApkName}`
    ].join('\n') + '\n';
    try {
      fs.appendFileSync(githubOutput, outputs, 'utf8');
    } catch (e) {
      console.warn('Could not write to GITHUB_OUTPUT:', e.message);
    }
  }

  console.log('\n✅ Packager completed successfully.');
}

main().catch(err => {
  console.error('\n❌ Release packaging error:', err);
  process.exit(1);
});
