package com.zenith.focus.feature.sensory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Sensory Dopamine Kill — Auto-Grayscale Controller.
 * Automatically turns the display monochrome during Nuclear Mode to strip colorful dopamine triggers.
 * Operates safely with 0 crashes if WRITE_SECURE_SETTINGS permission is absent.
 */
object GrayscaleController {

    private const val TAG = "GrayscaleController"
    const val ADB_GRANT_COMMAND = "adb shell pm grant com.zenith.focus android.permission.WRITE_SECURE_SETTINGS"

    // System settings keys for Android's Daltonizer (Monochromacy)
    const val DISPLAY_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled"
    const val DISPLAY_DALTONIZER = "accessibility_display_daltonizer"
    const val MONOCHROMACY_MODE = 0 // 0 = Monochromacy / Grayscale

    fun isPermissionGranted(context: Context): Boolean {
        return runCatching {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_SECURE_SETTINGS
            ) == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
    }

    fun isGrayscaleActive(context: Context): Boolean {
        return runCatching {
            val enabled = Settings.Secure.getInt(context.contentResolver, DISPLAY_DALTONIZER_ENABLED, 0)
            val mode = Settings.Secure.getInt(context.contentResolver, DISPLAY_DALTONIZER, -1)
            enabled == 1 && mode == MONOCHROMACY_MODE
        }.getOrDefault(false)
    }

    fun enableGrayscale(context: Context): Boolean {
        if (!isPermissionGranted(context)) {
            Log.w(TAG, "Cannot enable grayscale: WRITE_SECURE_SETTINGS permission not granted")
            return false
        }

        return try {
            val resolver = context.contentResolver
            Settings.Secure.putInt(resolver, DISPLAY_DALTONIZER, MONOCHROMACY_MODE)
            Settings.Secure.putInt(resolver, DISPLAY_DALTONIZER_ENABLED, 1)
            Log.i(TAG, "Monochrome display successfully activated")
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException enabling grayscale: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Unexpected error enabling grayscale: ${e.message}")
            false
        }
    }

    fun restoreColor(context: Context): Boolean {
        if (!isPermissionGranted(context)) {
            return false
        }

        return try {
            val resolver = context.contentResolver
            Settings.Secure.putInt(resolver, DISPLAY_DALTONIZER_ENABLED, 0)
            Log.i(TAG, "Full-color display successfully restored")
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException restoring color: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Unexpected error restoring color: ${e.message}")
            false
        }
    }

    fun applyGrayscaleIfEnabled(context: Context, isFeatureEnabled: Boolean): Boolean {
        return if (isFeatureEnabled && isPermissionGranted(context)) {
            enableGrayscale(context)
        } else {
            false
        }
    }
}
