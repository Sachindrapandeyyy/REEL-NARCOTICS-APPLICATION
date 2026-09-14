package com.zenith.focus.core.permission

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.zenith.focus.accessibility.service.ZenithAccessibilityService
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Declares the permission and capability categories used in Reel Narcotics.
 */
enum class Capability {
    /** Core accessibility inspection & kick-out service (REQUIRED) */
    ACCESSIBILITY,
    /** OS-level uninstall protection during active locks (RECOMMENDED/OPTIONAL) */
    DEVICE_ADMIN,
    /** Floating visual block card (OPTIONAL) */
    OVERLAY,
    /** In-app update APK installation (FEATURE-SCOPED) */
    INSTALL_UNKNOWN_APPS
}

/**
 * Immutable snapshot of system capability states.
 */
data class PermissionOrchestratorState(
    val isAccessibilityGranted: Boolean = false,
    val isDeviceAdminGranted: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val isInstallUnknownAppsGranted: Boolean = false
) {
    val isCoreOperational: Boolean
        get() = isAccessibilityGranted
}

/**
 * Centralized capability and permission orchestrator for Reel Narcotics.
 *
 * Guarantees:
 * 1. Queries live Android OS APIs directly — never assumes opening Settings implies permission was granted.
 * 2. Clearly distinguishes required vs optional capabilities.
 * 3. Provides clean standard Intents to guide users directly to the proper system screens.
 * 4. Exposes an observable StateFlow for automatic UI refreshes upon onResume.
 */
class PermissionOrchestrator(
    private val appContext: Context
) {
    private val _state = MutableStateFlow(queryLiveState(appContext))
    val state: StateFlow<PermissionOrchestratorState> = _state.asStateFlow()

    /**
     * Refreshes all capability states by querying the Android system.
     * Call on Activity onResume to capture changes when the user returns from system Settings.
     */
    fun refresh(context: Context = appContext): PermissionOrchestratorState {
        val newState = queryLiveState(context)
        _state.value = newState
        return newState
    }

    /**
     * Checks if the Reel Narcotics AccessibilityService is currently enabled in Android Settings.
     */
    fun isAccessibilityEnabled(context: Context = appContext): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val matchesManager = enabledServices.any {
            it.resolveInfo?.serviceInfo?.packageName == context.packageName
        }
        if (matchesManager) return true

        // Fallback: check Settings.Secure string
        return try {
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            val expectedComponent = ComponentName(context, ZenithAccessibilityService::class.java).flattenToString()
            val expectedShort = "${context.packageName}/${ZenithAccessibilityService::class.java.canonicalName}"
            enabledServicesSetting.contains(expectedComponent) || enabledServicesSetting.contains(expectedShort)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if Device Administrator is currently activated.
     */
    fun isDeviceAdminActive(context: Context = appContext): Boolean {
        return ZenithDeviceAdminReceiver.isAdminActive(context)
    }

    /**
     * Checks if Draw Over Other Apps permission is granted.
     */
    fun canDrawOverlays(context: Context = appContext): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Checks if the app is allowed to request package installations for updates.
     */
    fun canInstallUnknownApps(context: Context = appContext): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Queries the complete system state snapshot.
     */
    fun queryLiveState(context: Context = appContext): PermissionOrchestratorState {
        return PermissionOrchestratorState(
            isAccessibilityGranted = isAccessibilityEnabled(context),
            isDeviceAdminGranted = isDeviceAdminActive(context),
            isOverlayGranted = canDrawOverlays(context),
            isInstallUnknownAppsGranted = canInstallUnknownApps(context)
        )
    }

    // --- Standard System Intent Generators ---

    fun createAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun createDeviceAdminIntent(context: Context = appContext): Intent {
        return ZenithDeviceAdminReceiver.createAddAdminIntent(context)
    }

    fun createOverlaySettingsIntent(context: Context = appContext): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun createInstallUnknownAppsIntent(context: Context = appContext): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    fun createApplicationDetailsSettingsIntent(context: Context = appContext): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    // --- OEM Navigation Helpers ---

    val detectedBrand: DeviceBrand
        get() = OemNavigationManager.detectDeviceBrand()

    fun openAppInfo(context: Context = appContext): Boolean =
        OemNavigationManager.openAppInfo(context)

    fun openAccessibilitySettings(context: Context = appContext): Boolean =
        OemNavigationManager.openAccessibilitySettings(context)

    fun openOemAutostart(context: Context = appContext): Boolean =
        OemNavigationManager.openOemAutostart(context)

    fun openBatteryOptimization(context: Context = appContext): Boolean =
        OemNavigationManager.openBatteryOptimization(context)

    fun getOemGuidance(): OemGuidance =
        OemNavigationManager.getGuidance()
}
