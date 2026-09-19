package com.zenith.focus.accessibility.service

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.zenith.focus.ZenithApplication
import com.zenith.focus.accessibility.analyzer.HierarchyTraverser
import com.zenith.focus.accessibility.detector.DetectionEngine
import com.zenith.focus.accessibility.detector.TamperDetectionEngine
import com.zenith.focus.accessibility.overlay.OverlayWindowManager
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ZenithAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var detectionEngine: DetectionEngine
    private lateinit var overlayWindowManager: OverlayWindowManager

    private var lastEventTime = 0L
    private var lastPackageName = ""
    private var lastEjectTime = 0L

    companion object {
        private const val FAST_DEBOUNCE_MS = 60L
        private const val EJECT_COOLDOWN_MS = 600L

        // Core system communication and essential utility packages that must never be blocked or intercepted
        val ESSENTIAL_WHITELISTED_PACKAGES = setOf(
            "com.google.android.googlequicksearchbox", // Google App / Search
            "com.android.phone",
            "com.google.android.dialer",
            "com.android.incallui",
            "com.samsung.android.dialer",
            "com.samsung.android.incallui",
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.samsung.android.messaging",
            "com.google.android.gm", // Gmail
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "org.telegram.messenger.web",
            "com.google.android.calculator",
            "com.sec.android.app.popupcalculator",
            "com.google.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.google.android.apps.maps",
            // Xiaomi / POCO
            "com.android.contacts",
            "com.miui.calculator",
            // Vivo / iQOO
            "com.vivo.calculator",
            "com.android.BBKClock",
            // Oppo / Realme / OnePlus
            "com.coloros.calculator",
            "com.coloros.alarmclock",
            "com.oneplus.calculator",
            "com.oneplus.deskclock",
            // Transsion / Infinix / Tecno
            "com.transsion.calculator",
            "com.transsion.deskclock",
            "com.sh.smart.caller",
            "com.transsion.phonemaster"
        )

        fun isEssentialUtility(pkg: String): Boolean {
            if (pkg.isBlank()) return false
            if (ESSENTIAL_WHITELISTED_PACKAGES.contains(pkg)) return true
            return pkg.contains("dialer") || pkg.contains("incallui") || pkg.contains("telecom") ||
                   pkg.contains("calculator") || pkg.contains("deskclock") || pkg.contains("clockpackage") ||
                   pkg.contains("bbkclock") || pkg.contains("camera") || pkg.contains("gallery") ||
                   pkg.contains("alarmclock") || pkg.contains("emergency")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        detectionEngine = DetectionEngine(applicationContext)
        overlayWindowManager = OverlayWindowManager(this) {
            ejectToHomeScreen()
        }
        ServiceStateBroadcaster.updateConnected(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return
        val lowerPkg = pkg.lowercase(java.util.Locale.US)
        if (lowerPkg.isBlank() || lowerPkg == packageName.lowercase(java.util.Locale.US) ||
            lowerPkg.contains("launcher") || lowerPkg.contains("systemui") ||
            isEssentialUtility(lowerPkg)
        ) {
            return
        }

        val now = System.currentTimeMillis()

        // Debounce only extremely rapid duplicate events within 60ms
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (pkg == lastPackageName && now - lastEventTime < FAST_DEBOUNCE_MS) {
                return
            }
        }

        lastEventTime = now
        lastPackageName = pkg

        val rootNode = runCatching { rootInActiveWindow }.getOrNull() ?: event.source
        val screenContext = try {
            HierarchyTraverser.inspect(rootNode)
        } finally {
            if (Build.VERSION.SDK_INT < 34 && rootNode != null) {
                @Suppress("DEPRECATION")
                runCatching { rootNode.recycle() }
            }
        }

        serviceScope.launch {
            processScreenContext(pkg, screenContext)
        }
    }

    private suspend fun processScreenContext(pkg: String, screenContext: com.zenith.focus.accessibility.analyzer.ScreenContext) {
        val app = runCatching { ZenithApplication.instance }.getOrNull() ?: return
        val nuclearRepo = app.container.nuclearModeRepository
        val lockRepo = app.container.lockRepository
        val settingsRepo = app.container.settingsRepository
        val statsRepo = app.container.statisticsRepository

        val targetPkg = if (screenContext.packageName.isNotBlank()) screenContext.packageName.lowercase(java.util.Locale.US) else pkg.lowercase(java.util.Locale.US)
        if (targetPkg.isBlank() || targetPkg == packageName.lowercase(java.util.Locale.US) ||
            targetPkg.contains("launcher") || targetPkg.contains("systemui") ||
            isEssentialUtility(targetPkg)
        ) {
            return
        }

        val effectiveContext = if (screenContext.packageName.isBlank()) screenContext.copy(packageName = targetPkg) else screenContext

        val nuclearSession = nuclearRepo.session.value
        val lockState = lockRepo.lockState.value
        val now = System.currentTimeMillis()
        val elapsed = android.os.SystemClock.elapsedRealtime()
        val isNuclear = nuclearSession.isCurrentlyActive(now, elapsed)

        // --- ANTI-UNINSTALL & ANTI-TAMPER SHIELD ---
        if (isNuclear || lockState.isCurrentlyActive(now)) {
            val tamperResult = TamperDetectionEngine.evaluate(effectiveContext)
            if (tamperResult.isTamperAttempt) {
                // DO NOT DEBOUNCE TAMPER ATTEMPTS!
                // Any attempt to uninstall or disable accessibility during nuclear/lock mode must be instantly ejected.
                lastEjectTime = now

                // 1. Instant global dismiss & back to home actions
                performGlobalAction(GLOBAL_ACTION_BACK)
                performGlobalAction(GLOBAL_ACTION_HOME)

                // 2. Instant physical haptic shock
                triggerHapticAlert()

                // 3. If Nuclear Mode is active, IMMEDIATELY lock the screen via Device Admin.
                // This shuts the screen off instantaneously, eliminating any touch window for the user.
                if (isNuclear) {
                    runCatching {
                        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                        if (ZenithDeviceAdminReceiver.isAdminActive(this@ZenithAccessibilityService)) {
                            dpm?.lockNow()
                        }
                    }
                }

                // 4. Force home intent and show alert toast on main thread
                withContext(Dispatchers.Main) {
                    runCatching {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(homeIntent)
                    }
                    val toastMessage = if (isNuclear) {
                        "🔒 NUCLEAR FOCUS IS ON: Reel Narcotics cannot be modified or turned off until your session expires!"
                    } else {
                        "🔒 FOCUS LOCK ACTIVE: Reel Narcotics settings and removal are locked during focus session."
                    }
                    Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG).show()
                }

                statsRepo.recordBlockEvent(
                    BlockEvent(
                        timestamp = now,
                        packageName = targetPkg,
                        category = ContentCategory.SYSTEM_TAMPER,
                        confidence = 1.0f,
                        ruleId = "tamper_protection"
                    )
                )
                return
            }
        }

        val config = settingsRepo.protectionConfig.value
        val habitConfig = settingsRepo.habitConfig.value
        val result = detectionEngine.evaluate(effectiveContext, config)

        if (result.isBlocked) {

            val isEnforced = com.zenith.focus.accessibility.policy.NuclearProtectionPolicy.shouldBlock(
                result = result,
                nuclearSession = nuclearSession,
                lockState = lockState,
                config = config,
                nowWallClock = now,
                nowElapsedRealtime = elapsed,
                habitConfig = habitConfig
            )

            if (!isEnforced) {
                return
            }

            if (now - lastEjectTime < EJECT_COOLDOWN_MS) {
                return
            }
            lastEjectTime = now

            // 1. PHYSICAL HAPTIC SHOCK
            triggerHapticAlert()

            // 2. SURGICAL SHORT CLOSE: Close only the active short/reel without killing host app
            withContext(Dispatchers.Main) {
                if (result.category == ContentCategory.ADULT_WEBSITE || result.category == ContentCategory.ADULT_KEYWORD) {
                    ejectToHomeScreen()
                    Toast.makeText(
                        applicationContext,
                        "🛡️ Explicit content blocked. Exiting to Home.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    closeActiveShortsOrReel()
                    val isBedtime = habitConfig.isBedtimeActive(now)
                    val feedbackText = when {
                        isNuclear -> "☢️ NUCLEAR LOCK: Reel/Short closed."
                        lockState.isCurrentlyActive(now) -> "🔒 FOCUS LOCK: Reel/Short closed."
                        isBedtime -> "🌙 BEDTIME SHIELD: Sleep is your superpower. Put your phone down!"
                        else -> "🛡️ REEL BLOCKED: Reel/Short closed."
                    }
                    Toast.makeText(
                        applicationContext,
                        feedbackText,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // 3. Record block event in local database
            statsRepo.recordBlockEvent(
                BlockEvent(
                    timestamp = System.currentTimeMillis(),
                    packageName = pkg,
                    category = result.category,
                    confidence = result.confidence,
                    ruleId = result.ruleId
                )
            )
        }
    }

    private fun closeActiveShortsOrReel() {
        // Press BACK to exit the short/reel player while keeping the main app open
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun ejectToHomeScreen() {
        // Press BACK to stop playback and immediately return to phone Home Screen
        performGlobalAction(GLOBAL_ACTION_BACK)
        performGlobalAction(GLOBAL_ACTION_HOME)
        runCatching {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(homeIntent)
        }
    }

    private fun triggerHapticAlert() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 180, 80, 250)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(400L)
        }
    }

    override fun onInterrupt() {
        overlayWindowManager.dismissOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceStateBroadcaster.updateConnected(false)
        overlayWindowManager.dismissOverlay()
        serviceScope.cancel()
    }
}
