package com.zenith.focus.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

class UpdateInstaller(
    private val context: Context
) {
    /**
     * Checks whether the application has permission to request package installation (API 26+).
     */
    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Returns an intent to open the system settings screen for enabling unknown app sources.
     */
    fun getManageUnknownAppSourcesIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    /**
     * Initiates the official Android PackageInstaller confirmation prompt for the verified APK file.
     * Android will display the official system user confirmation dialog.
     */
    fun install(apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists()) {
                return Result.failure(UpdateException.InstallationException("APK file not found: ${apkFile.absolutePath}"))
            }

            if (!canRequestPackageInstalls()) {
                context.startActivity(getManageUnknownAppSourcesIntent())
                return Result.failure(
                    UpdateException.InstallationException("Permission to install unknown apps is required. Please enable it and tap install.")
                )
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(UpdateException.InstallationException("Failed to launch PackageInstaller: ${e.localizedMessage}", e))
        }
    }
}
