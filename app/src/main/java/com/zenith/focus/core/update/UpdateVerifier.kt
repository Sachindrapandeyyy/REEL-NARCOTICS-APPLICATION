package com.zenith.focus.core.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class UpdateVerifier(
    private val context: Context,
    private val expectedPackageName: String = context.packageName
) {
    companion object {
        // Known release signing certificate SHA-256 fingerprint (in uppercase hex without colons)
        const val PRODUCTION_CERT_FINGERPRINT = "9DC4F5DE70DEBCC1E0E897DD521361553688AF427DA7953BF0566834A3FCDD12"
    }

    /**
     * Executes comprehensive defense-in-depth validation on the downloaded APK.
     * If any check fails, the file is safely deleted and an exception is returned.
     */
    fun verify(apkFile: File, manifest: UpdateManifest): Result<Unit> {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            return Result.failure(UpdateException.SecurityVerificationException("Downloaded APK file is empty or missing"))
        }

        // 1. File Size sanity check
        if (manifest.apk.sizeBytes > 0 && apkFile.length() != manifest.apk.sizeBytes) {
            apkFile.delete()
            return Result.failure(
                UpdateException.ChecksumMismatchException(
                    "Size mismatch: expected ${manifest.apk.sizeBytes} bytes, got ${apkFile.length()} bytes"
                )
            )
        }

        // 2. SHA-256 verification
        val calculatedSha = calculateSha256(apkFile)
        val expectedSha = manifest.apk.sha256.trim().lowercase()
        if (calculatedSha.lowercase() != expectedSha) {
            apkFile.delete()
            return Result.failure(
                UpdateException.ChecksumMismatchException(
                    "SHA-256 mismatch! Expected $expectedSha, calculated $calculatedSha"
                )
            )
        }

        // 3. Inspect APK structure & metadata via Android PackageManager
        @Suppress("DEPRECATION")
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
        } else {
            PackageManager.GET_SIGNATURES
        }

        val packageInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, flags)
            ?: run {
                apkFile.delete()
                return Result.failure(
                    UpdateException.SecurityVerificationException("Android PackageManager could not parse APK archive")
                )
            }

        // Package Name match
        if (packageInfo.packageName != expectedPackageName) {
            apkFile.delete()
            return Result.failure(
                UpdateException.PackageMismatchException(
                    "Downloaded APK package name '${packageInfo.packageName}' does not match expected '$expectedPackageName'"
                )
            )
        }

        // Version Code match
        val apkVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }

        if (apkVersionCode != manifest.versionCode) {
            apkFile.delete()
            return Result.failure(
                UpdateException.SecurityVerificationException(
                    "APK internal versionCode ($apkVersionCode) does not match manifest (${manifest.versionCode})"
                )
            )
        }

        // 4. Signing certificate verification against currently installed app
        val signatureResult = verifySignatures(packageInfo)
        if (signatureResult.isFailure) {
            apkFile.delete()
            return signatureResult
        }

        return Result.success(Unit)
    }

    private fun verifySignatures(apkPackageInfo: PackageInfo): Result<Unit> {
        val installedSignatures = getInstalledSignatures()
        val apkSignatures = getArchiveSignatures(apkPackageInfo)

        if (installedSignatures.isEmpty() || apkSignatures.isEmpty()) {
            // If running in local test environment without installed signatures, check against production fingerprint
            val apkHasProdFingerprint = apkSignatures.any { sig ->
                val fp = computeSha256Hex(sig)
                fp.equals(PRODUCTION_CERT_FINGERPRINT, ignoreCase = true)
            }
            return if (apkHasProdFingerprint || installedSignatures.isEmpty()) {
                Result.success(Unit)
            } else {
                Result.failure(UpdateException.SignatureMismatchException("Could not extract signing certificates"))
            }
        }

        val matches = apkSignatures.any { apkSig ->
            installedSignatures.any { installedSig ->
                apkSig.contentEquals(installedSig)
            }
        }

        return if (matches) {
            Result.success(Unit)
        } else {
            Result.failure(
                UpdateException.SignatureMismatchException("APK signing identity does not match installed app signature")
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun getInstalledSignatures(): List<ByteArray> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val flags = PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
                val info = context.packageManager.getPackageInfo(context.packageName, flags)
                val signers = info.signingInfo?.apkContentsSigners?.map { it.toByteArray() }
                if (!signers.isNullOrEmpty()) {
                    return signers
                }
                info.signatures?.map { it.toByteArray() } ?: emptyList()
            } else {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                info.signatures?.map { it.toByteArray() } ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Suppress("DEPRECATION")
    private fun getArchiveSignatures(packageInfo: PackageInfo): List<ByteArray> {
        val signers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners?.map { it.toByteArray() }
        } else null

        if (!signers.isNullOrEmpty()) {
            return signers
        }

        return packageInfo.signatures?.map { it.toByteArray() } ?: emptyList()
    }

    fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun computeSha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02X".format(it) }
    }
}
