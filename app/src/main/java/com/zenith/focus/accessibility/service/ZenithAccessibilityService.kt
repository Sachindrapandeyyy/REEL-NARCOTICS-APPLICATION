package com.zenith.focus.accessibility.service

import android.accessibilityservice.AccessibilityService
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
        if (pkg.isBlank() || pkg == packageName || pkg.contains("launcher") || pkg.contains("systemui")) {
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

        serviceScope.launch {
            processEventSafely(pkg, rootNode)
        }
    }

    private suspend fun processEventSafely(pkg: String, node: android.view.accessibility.AccessibilityNodeInfo?) {
        val app = runCatching { ZenithApplication.instance }.getOrNull() ?: return
        val nuclearRepo = app.container.nuclearModeRepository
        val lockRepo = app.container.lockRepository
        val settingsRepo = app.container.settingsRepository
        val statsRepo = app.container.statisticsRepository

        val targetNode = node ?: runCatching { rootInActiveWindow }.getOrNull() ?: return
        val screenContext = HierarchyTraverser.inspect(targetNode)

        val targetPkg = screenContext.packageName.lowercase()
        if (targetPkg.isBlank() || targetPkg == packageName.lowercase() || targetPkg.contains("launcher") || targetPkg.contains("systemui")) {
            return
        }

        val nuclearSession = nuclearRepo.session.value
        val lockState = lockRepo.lockState.value
        val now = System.currentTimeMillis()
        val elapsed = android.os.SystemClock.elapsedRealtime()
        val isNuclear = nuclearSession.isCurrentlyActive(now, elapsed)

        // --- ANTI-UNINSTALL & ANTI-TAMPER SHIELD ---
        if (isNuclear || lockState.isCurrentlyActive(now)) {
            val tamperResult = TamperDetectionEngine.evaluate(screenContext)
            if (tamperResult.isTamperAttempt) {
                if (now - lastEjectTime < EJECT_COOLDOWN_MS) {
                    return
                }
                lastEjectTime = now

                triggerHapticAlert()
                withContext(Dispatchers.Main) {
                    ejectToHomeScreen()
                    val toastMessage = if (isNuclear) {
                        "🔒 NUCLEAR FOCUS IS ON: Reel Narcotics cannot be deleted or modified until your session expires!"
                    } else {
                        "🔒 FOCUS LOCK ACTIVE: Reel Narcotics settings and removal are locked during focus session."
                    }
                    Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG).show()
                }

                statsRepo.recordBlockEvent(
                    BlockEvent(
                        timestamp = now,
                        packageName = pkg,
                        category = ContentCategory.SYSTEM_TAMPER,
                        confidence = 1.0f,
                        ruleId = "tamper_protection"
                    )
                )
                return
            }
        }

        val config = settingsRepo.protectionConfig.value
        val result = detectionEngine.evaluate(screenContext, config)

        if (result.isBlocked) {

            val isEnforced = com.zenith.focus.accessibility.policy.NuclearProtectionPolicy.shouldBlock(
                result = result,
                nuclearSession = nuclearSession,
                lockState = lockState,
                config = config,
                nowWallClock = now,
                nowElapsedRealtime = elapsed
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

            // 2. S++ INSTANT KICK-OUT: Force exit directly to phone's Home Screen
            withContext(Dispatchers.Main) {
                ejectToHomeScreen()
                val statusMsg = if (isNuclear) "☢️ NUCLEAR LOCK (STRICT RESTRICTION)" else "🔒 STANDARD FOCUS LOCK (RESTRICTION ACTIVE)"
                Toast.makeText(
                    applicationContext,
                    "$statusMsg\nShort-form loop terminated. Keep focusing!",
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun ejectToHomeScreen() {
        // First, press BACK to terminate Shorts/Reel playback and prevent PiP
        performGlobalAction(GLOBAL_ACTION_BACK)
        // Immediately kick user out to the Android Home Screen
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
