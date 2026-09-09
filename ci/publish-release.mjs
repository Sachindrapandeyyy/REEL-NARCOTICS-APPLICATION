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

  const defaultApkPath = path.join(projectRoot, 'app', 'build', 'outputs', 'apk', 'release', 'app-release.apk');
  if (!fs.existsSync(defaultApkPath)) {
    throw new Error(`Release APK not found at: ${defaultApkPath}. Run './gradlew assembleRelease' first.`);
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
  // Default public Vercel Blob store endpoint for 'reel-narcotics-releases'
  const publicBlobBaseUrl = process.env.PUBLIC_BLOB_BASE_URL ||
    `https://reel-narcotics-releases.public.blob.vercel-storage.com`;
  const remoteApkUrl = `${publicBlobBaseUrl}/releases/${versionInfo.versionName}/${versionedApkName}`;

  // Read release notes from Git log or environment if available
  const releaseNotes = [
    `Reel Narcotics Release v${versionInfo.versionName} (Build ${versionInfo.versionCode})`,
    "Scandinavian Kinfolk Organic Earth & Linen Minimalist UI",
    "Complete Day & Night Mode with 1-tap quick switch and persistent settings",
    "High-resolution native landscape artwork (Day & Night)",
    "Selective Nuclear Lock with individual feed toggles",
    "Extended Focus Timer steppers from 0 to 90 Days with 5-minute precision",
    "Production auto-update checking with SHA-256 cryptographic verification",
    "100% Offline blocking engine with zero telemetry"
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

  // 3. Vercel Blob Upload
  const blobToken = process.env.BLOB_READ_WRITE_TOKEN;
  if (blobToken && !isDryRun) {
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
      console.error('❌ Failed to upload to Vercel Blob:', err.message);
      if (process.env.CI) {
        throw err;
      }
    }
  } else {
    if (!blobToken) {
      console.log('\nℹ️ BLOB_READ_WRITE_TOKEN not set in environment.');
      console.log('Release artifacts staged locally in: ' + distDir);
      console.log('To publish to Vercel Blob, provide BLOB_READ_WRITE_TOKEN in CI/environment.');
    } else {
      console.log('\nℹ️ Dry run complete. No files uploaded.');
    }
  }

  // 4. Output GitHub Actions step parameters if running in GitHub Actions
  const githubOutput = process.env.GITHUB_OUTPUT;
  if (githubOutput && fs.existsSync(githubOutput)) {
    const outputs = [
      `versionName=${versionInfo.versionName}`,
      `versionCode=${versionInfo.versionCode}`,
      `apkSha256=${apkSha256}`,
      `apkSizeBytes=${apkSizeBytes}`,
      `apkPath=${stagedApkPath}`,
      `updateJsonPath=${stagedUpdateJsonPath}`,
      `apkName=${versionedApkName}`
    ].join('\n') + '\n';
    fs.appendFileSync(githubOutput, outputs, 'utf8');
  }

  console.log('\n✅ Packager completed successfully.');
}

main().catch(err => {
  console.error('\n❌ Release packaging error:', err);
  process.exit(1);
});
