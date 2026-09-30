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
    private var lastBlockedPkg = ""
    private var consecutiveBlockCount = 0
    private var lastBlockTimestamp = 0L
    private var lastTargetAppLongPressTime = 0L

    companion object {
        private const val FAST_DEBOUNCE_MS = 60L
        private const val EJECT_COOLDOWN_MS = 750L

        // Core system communication, essential utility, and educational packages that must never be blocked or intercepted
        val ESSENTIAL_WHITELISTED_PACKAGES = setOf(
            "com.google.android.googlequicksearchbox", // Google App / Search
            "com.android.phone",
            "com.google.android.dialer",
            "com.google.android.incallui",
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
            // Educational and learning platforms
            "xyz.penpencil.physicswala",
            "com.physicswallah",
            "com.unacademyapp",
            "org.khanacademy.android",
            "org.coursera.android",
            "com.udemy.android",
            "com.byjus.thelearningapp",
            "com.vedantu.student",
            "com.doubtnut",
            "com.allen.allenapp",
            "com.testbook.tbapp",
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
            "com.sh.smart.caller"
        )

        fun isEssentialUtility(pkg: String): Boolean {
            if (pkg.isBlank()) return false
            if (ESSENTIAL_WHITELISTED_PACKAGES.contains(pkg)) return true
            return pkg.contains("dialer") || pkg.contains("incallui") || pkg.contains("telecom") ||
                   pkg.contains("calculator") || pkg.contains("deskclock") || pkg.contains("clockpackage") ||
                   pkg.contains("bbkclock") || pkg.contains("camera") || pkg.contains("gallery") ||
                   pkg.contains("alarmclock") || pkg.contains("emergency") ||
                   pkg.contains("penpencil") || pkg.contains("physicswalla") || pkg.contains("unacademy") ||
                   pkg.contains("khanacademy") || pkg.contains("coursera") || pkg.contains("udemy") ||
                   pkg.contains("byjus") || pkg.contains("vedantu") || pkg.contains("doubtnut") ||
                   pkg.contains("allen") || pkg.contains("testbook")
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
        if (lowerPkg.isBlank() || lowerPkg == packageName.lowercase(java.util.Locale.US) || lowerPkg.contains("systemui")) {
            return
        }

        val now = System.currentTimeMillis()

        val eventText = run {
            val sb = StringBuilder()
            event.text.forEach { t -> if (!t.isNullOrBlank()) sb.append(t).append(" ") }
            event.contentDescription?.let { d -> if (d.isNotBlank()) sb.append(d).append(" ") }
            val src = runCatching { event.source }.getOrNull()
            if (src != null) {
                src.text?.let { if (!it.isNullOrBlank()) sb.append(it).append(" ") }
                src.contentDescription?.let { if (it.isNotBlank()) sb.append(it).append(" ") }
            }
            sb.toString().lowercase(java.util.Locale.US)
        }

        val app = runCatching { ZenithApplication.instance }.getOrNull()
        val nowTime = System.currentTimeMillis()
        val elapsed = android.os.SystemClock.elapsedRealtime()
        val isNuclear = app?.container?.nuclearModeRepository?.session?.value?.isCurrentlyActive(nowTime, elapsed) == true
        val isLockActive = app?.container?.lockRepository?.lockState?.value?.isCurrentlyActive(nowTime) == true

        // 1. LAYER 1: Prevent Long-Press / Dragging / Context Menus on Reel Narcotics
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            val mentionsTarget = TamperDetectionEngine.mentionsTargetApp(eventText) || run {
                val src = runCatching { event.source }.getOrNull()
                if (src != null) {
                    val sText = src.text?.toString() ?: ""
                    val sDesc = src.contentDescription?.toString() ?: ""
                    TamperDetectionEngine.mentionsTargetApp(sText) || TamperDetectionEngine.mentionsTargetApp(sDesc)
                } else false
            }

            if (mentionsTarget) {
                lastTargetAppLongPressTime = now
                if (isNuclear || isLockActive) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    triggerHapticAlert()
                    if (isNuclear) {
                        runCatching {
                            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                            if (ZenithDeviceAdminReceiver.isAdminActive(this@ZenithAccessibilityService)) {
                                dpm?.lockNow()
                            }
                        }
                    }
                    if (nowTime - lastEjectTime >= 500L) {
                        lastEjectTime = nowTime
                        serviceScope.launch(Dispatchers.Main) {
                            val msg = if (isNuclear) {
                                "🔒 NUCLEAR LOCK ACTIVE: Reel Narcotics cannot be dragged, frozen, or modified!"
                            } else {
                                "🔒 FOCUS LOCK ACTIVE: Reel Narcotics cannot be modified or frozen."
                            }
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                    return
                }
            }
        }

        // 2. LAYER 2: In Nuclear Mode or Focus Lock, completely block opening ANY Freezer UI or Folder
        if (isNuclear || isLockActive) {
            val isFreezerAttempt = TamperDetectionEngine.isFreezerActionOrFolder(lowerPkg, event, eventText)
            if (isFreezerAttempt) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                performGlobalAction(GLOBAL_ACTION_BACK)
                triggerHapticAlert()
                if (isNuclear) {
                    runCatching {
                        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                        if (ZenithDeviceAdminReceiver.isAdminActive(this@ZenithAccessibilityService)) {
                            dpm?.lockNow()
                        }
                    }
                }
                if (nowTime - lastEjectTime >= 500L) {
                    lastEjectTime = nowTime
                    serviceScope.launch(Dispatchers.Main) {
                        val msg = if (isNuclear) {
                            "🔒 NUCLEAR LOCK ACTIVE: Freezer is completely blocked during Nuclear Focus!"
                        } else {
                            "🔒 FOCUS LOCK ACTIVE: Freezer is disabled during active focus sessions."
                        }
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                    }
                    serviceScope.launch {
                        app?.container?.statisticsRepository?.recordBlockEvent(
                            BlockEvent(
                                timestamp = nowTime,
                                packageName = lowerPkg,
                                category = ContentCategory.SYSTEM_TAMPER,
                                confidence = 1.0f,
                                ruleId = "nuclear_freezer_block"
                            )
                        )
                    }
                }
                return
            }
        }

        // 3. LAYER 3: FAST-PATH ANTI-FREEZER & TAMPER SHIELD:
        // If user clicks on Reel Narcotics inside a Freezer app, or clicks "Freeze" / "To Freezer" in a menu,
        // instantly eject to Home Screen in sub-5ms!
        if (TamperDetectionEngine.isDirectFreezerTamper(lowerPkg, event, eventText, lastTargetAppLongPressTime, now)) {
            ejectToHomeScreen()
            triggerHapticAlert()
            if (now - lastEjectTime >= 500L) {
                lastEjectTime = now
                serviceScope.launch(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "🔒 Reel Narcotics cannot be added to Freezer or frozen.", Toast.LENGTH_SHORT).show()
                }
                serviceScope.launch {
                    val appInstance = runCatching { ZenithApplication.instance }.getOrNull()
                    appInstance?.container?.statisticsRepository?.recordBlockEvent(
                        BlockEvent(
                            timestamp = now,
                            packageName = lowerPkg,
                            category = ContentCategory.SYSTEM_TAMPER,
                            confidence = 1.0f,
                            ruleId = "fast_path_freezer_tamper"
                        )
                    )
                }
            }
            return
        }

        // FAST-PATH APP LOCK SHIELD:
        // If this package is locked in App Lock, instantly eject it to Home Screen without waiting for heavy hierarchy inspection!
        if (app != null) {
            val nuclearRepo = app.container.nuclearModeRepository
            val isNuclear = nuclearRepo.session.value.isCurrentlyActive(nowTime, elapsed)
            val appLockConfig = app.container.appLockRepository.appLockConfig.value

            if (appLockConfig.isPackageLocked(lowerPkg, isNuclear)) {
                val rule = appLockConfig.getRule(lowerPkg)
                val isPermanent = rule?.lockMode == com.zenith.focus.domain.model.AppLockMode.PERMANENT
                val appTitle = rule?.appName ?: lowerPkg

                // 1. Instant global eject to Home Screen
                ejectToHomeScreen()

                // 2. Physical haptic alert
                triggerHapticAlert()

                // 3. User toast & statistics with debounce
                if (nowTime - lastEjectTime >= EJECT_COOLDOWN_MS) {
                    lastEjectTime = nowTime
                    serviceScope.launch(Dispatchers.Main) {
                        val toastMsg = if (isPermanent) {
                            "🔒 PERMANENT LOCK: $appTitle is locked 24/7."
                        } else {
                            "☢️ NUCLEAR LOCK: $appTitle is locked during your active Nuclear session!"
                        }
                        Toast.makeText(applicationContext, toastMsg, Toast.LENGTH_SHORT).show()
                    }

                    serviceScope.launch {
                        app.container.statisticsRepository.recordBlockEvent(
                            BlockEvent(
                                timestamp = nowTime,
                                packageName = lowerPkg,
                                category = ContentCategory.APP_LOCK,
                                confidence = 1.0f,
                                ruleId = if (isPermanent) "app_lock_permanent" else "app_lock_nuclear"
                            )
                        )
                    }
                }
                return
            }
        }

        // FAST-PATH BROWSER ADULT SHIELD:
        // If an explicit adult search or domain is present directly in the event text or description for a browser,
        // instantly eject to Home Screen without waiting for DOM hierarchy traversal!
        if (com.zenith.focus.accessibility.detector.BrowserUrlDetector.isBrowserPackage(lowerPkg)) {
            val appInstance = runCatching { ZenithApplication.instance }.getOrNull()
            if (appInstance != null) {
                val protectionConfig = appInstance.container.settingsRepository.protectionConfig.value
                val isAdultProtectionEnabled = protectionConfig.blockAdultWebsites || protectionConfig.blockAdultKeywords
                if (isAdultProtectionEnabled) {
                    val eventHasAdultText = run {
                        val texts = mutableListOf<String>()
                        event.text.forEach { t -> if (!t.isNullOrBlank()) texts.add(t.toString()) }
                        event.contentDescription?.let { d -> if (d.isNotBlank()) texts.add(d.toString()) }
                        texts.any { t -> com.zenith.focus.accessibility.detector.BrowserUrlDetector.hasExplicitAdultContent(t) }
                    }

                    if (eventHasAdultText) {
                        ejectToHomeScreen()
                        triggerHapticAlert()
                        val nowTime = System.currentTimeMillis()
                        if (nowTime - lastEjectTime >= 500L) {
                            lastEjectTime = nowTime
                            serviceScope.launch(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "🛡️ Explicit content blocked. Exiting to Home.", Toast.LENGTH_SHORT).show()
                            }
                            serviceScope.launch {
                                appInstance.container.statisticsRepository.recordBlockEvent(
                                    BlockEvent(
                                        timestamp = nowTime,
                                        packageName = lowerPkg,
                                        category = ContentCategory.ADULT_WEBSITE,
                                        confidence = 1.0f,
                                        ruleId = "adult_fast_event_match"
                                    )
                                )
                            }
                        }
                        return
                    }
                }
            }
        }

        // Launcher & Essential Utility performance guard:
        // Launchers and essential utilities (calculators/clocks) only need heavy hierarchy inspection
        // if a target app was recently long-pressed or the event indicates a freezer/tamper UI.
        // NOTE: During active Nuclear Mode or Focus Lock, we NEVER skip inspection!
        val isLauncherPkg = lowerPkg.contains("launcher")
        val isEssential = isEssentialUtility(lowerPkg)
        if ((isLauncherPkg || isEssential) && !isNuclear && !isLockActive) {
            val recentLongPress = (now - lastTargetAppLongPressTime) < 10000L
            val className = event.className?.toString()?.lowercase(java.util.Locale.US) ?: ""
            val isFreezerCandidate = TamperDetectionEngine.isFreezerPackage(lowerPkg) ||
                    TamperDetectionEngine.hasFreezerKeyword(eventText) ||
                    className.contains("freezer") || className.contains("shortcut") ||
                    className.contains("menu") || className.contains("popup") ||
                    className.contains("folder")

            if (!recentLongPress && !isFreezerCandidate) {
                return
            }
        }

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
        if (targetPkg.isBlank() || targetPkg == packageName.lowercase(java.util.Locale.US) || targetPkg.contains("systemui")) {
            return
        }

        val effectiveContext = if (screenContext.packageName.isBlank()) screenContext.copy(packageName = targetPkg) else screenContext

        val nuclearSession = nuclearRepo.session.value
        val lockState = lockRepo.lockState.value
        val now = System.currentTimeMillis()
        val elapsed = android.os.SystemClock.elapsedRealtime()
        val isNuclear = nuclearSession.isCurrentlyActive(now, elapsed)
        val isLockActive = lockState.isCurrentlyActive(now)

        // --- ANTI-UNINSTALL & ANTI-TAMPER SHIELD ---
        // Freezer & Deep Sleep protection is ALWAYS active (freezing cuts off accessibility and suspends all protection).
        // Settings uninstall/force stop is guarded during active nuclear or focus sessions.
        val isFreezerScreen = TamperDetectionEngine.isFreezerScreenContext(effectiveContext)
        val isNuclearFreezerBlock = (isNuclear || isLockActive) && isFreezerScreen
        val tamperResult = TamperDetectionEngine.evaluate(effectiveContext)
        val isFreezerAttempt = isNuclearFreezerBlock || (tamperResult.isTamperAttempt && (
            tamperResult.reason.contains("Freezer", ignoreCase = true) ||
            tamperResult.reason.contains("Freeze", ignoreCase = true) ||
            tamperResult.reason.contains("Sleep", ignoreCase = true) ||
            tamperResult.reason.contains("Restrict", ignoreCase = true)
        ))
        val shouldBlockTamper = isNuclearFreezerBlock || (tamperResult.isTamperAttempt && (isFreezerAttempt || isNuclear || isLockActive))

        if (shouldBlockTamper) {
            android.util.Log.e("ZENITH_TAMPER", "Tamper triggered! reason=${tamperResult.reason}, isNuclearFreezer=$isNuclearFreezerBlock, pkg=$targetPkg")
            // DO NOT DEBOUNCE TAMPER ATTEMPTS!
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
                val toastMessage = if (isNuclear && (isNuclearFreezerBlock || isFreezerAttempt)) {
                    "🔒 NUCLEAR LOCK ACTIVE: Freezer is completely blocked during Nuclear Focus!"
                } else if (isFreezerAttempt) {
                    "🔒 Reel Narcotics cannot be added to Freezer or put to sleep."
                } else if (isNuclear) {
                    "🔒 NUCLEAR FOCUS IS ON: Reel Narcotics cannot be frozen, modified, or turned off until your session expires!"
                } else {
                    "🔒 FOCUS LOCK ACTIVE: Reel Narcotics cannot be frozen, modified, or removed during your focus session."
                }
                Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG).show()
            }

            statsRepo.recordBlockEvent(
                BlockEvent(
                    timestamp = now,
                    packageName = targetPkg,
                    category = ContentCategory.SYSTEM_TAMPER,
                    confidence = 1.0f,
                    ruleId = if (isFreezerAttempt) "tamper_freezer_intercepted" else "tamper_protection"
                )
            )
            return
        }

        // Launchers and essential utilities (calculators/clocks) don't have reels, shorts, or adult websites.
        // Once tamper/freezer check has passed, safely skip further processing.
        if (targetPkg.contains("launcher") || isEssentialUtility(targetPkg)) {
            return
        }

        // --- APP LOCK & BLOCKER SHIELD ENFORCEMENT ---
        val appLockRepo = app.container.appLockRepository
        val appLockConfig = appLockRepo.appLockConfig.value

        if (appLockConfig.isPackageLocked(targetPkg, isNuclear)) {
            val rule = appLockConfig.getRule(targetPkg)
            val isPermanent = rule?.lockMode == com.zenith.focus.domain.model.AppLockMode.PERMANENT
            val appTitle = rule?.appName ?: targetPkg

            // Instant home ejection and tactile feedback
            ejectToHomeScreen()
            triggerHapticAlert()

            if (now - lastEjectTime >= EJECT_COOLDOWN_MS) {
                lastEjectTime = now

                withContext(Dispatchers.Main) {
                    val toastMessage = if (isPermanent) {
                        "🔒 PERMANENT LOCK: $appTitle is locked 24/7."
                    } else {
                        "☢️ NUCLEAR LOCK: $appTitle is locked during your active Nuclear session!"
                    }
                    Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
                }

                statsRepo.recordBlockEvent(
                    BlockEvent(
                        timestamp = now,
                        packageName = targetPkg,
                        category = ContentCategory.APP_LOCK,
                        confidence = 1.0f,
                        ruleId = if (isPermanent) "app_lock_permanent" else "app_lock_nuclear"
                    )
                )
            }
            return
        }

        val config = settingsRepo.protectionConfig.value
        val habitConfig = settingsRepo.habitConfig.value

        // When Nuclear Mode is active, enforce all active nuclear session categories in effective config
        val effectiveProtectionConfig = if (isNuclear) {
            val cats = if (nuclearSession.enabledCategories.isNotEmpty()) {
                nuclearSession.enabledCategories
            } else {
                setOf(
                    ContentCategory.YOUTUBE_SHORTS,
                    ContentCategory.INSTAGRAM_REELS,
                    ContentCategory.SNAPCHAT_SPOTLIGHT,
                    ContentCategory.FACEBOOK_REELS,
                    ContentCategory.TIKTOK,
                    ContentCategory.OTHER_SHORT_VIDEO,
                    ContentCategory.ADULT_WEBSITE,
                    ContentCategory.ADULT_KEYWORD
                )
            }
            config.copy(
                blockYouTubeShorts = cats.contains(ContentCategory.YOUTUBE_SHORTS),
                blockInstagramReels = cats.contains(ContentCategory.INSTAGRAM_REELS),
                blockSnapchatSpotlight = cats.contains(ContentCategory.SNAPCHAT_SPOTLIGHT),
                blockFacebookReels = cats.contains(ContentCategory.FACEBOOK_REELS),
                blockTikTok = cats.contains(ContentCategory.TIKTOK),
                blockOtherShortVideo = cats.contains(ContentCategory.OTHER_SHORT_VIDEO),
                blockAdultWebsites = cats.contains(ContentCategory.ADULT_WEBSITE),
                blockAdultKeywords = cats.contains(ContentCategory.ADULT_KEYWORD) || cats.contains(ContentCategory.ADULT_WEBSITE)
            )
        } else {
            config
        }

        val result = detectionEngine.evaluate(effectiveContext, effectiveProtectionConfig)
        android.util.Log.e("ZENITH_EVAL", "pkg=$targetPkg, blocked=${result.isBlocked}, reason=${result.reason}")

        if (result.isBlocked) {

            val isEnforced = com.zenith.focus.accessibility.policy.NuclearProtectionPolicy.shouldBlock(
                result = result,
                nuclearSession = nuclearSession,
                lockState = lockState,
                config = effectiveProtectionConfig,
                nowWallClock = now,
                nowElapsedRealtime = elapsed,
                habitConfig = habitConfig
            )

            if (!isEnforced) {
                return
            }

            val isAdultCategory = result.category == ContentCategory.ADULT_WEBSITE || 
                                  result.category == ContentCategory.ADULT_KEYWORD

            // Adult content blocks must never be delayed or dropped by the surgical reel cooldown
            if (!isAdultCategory && now - lastEjectTime < EJECT_COOLDOWN_MS) {
                return
            }
            lastEjectTime = now

            val isDedicatedTab = result.reason.contains("tab actively selected", ignoreCase = true)

            // When closeActiveShortsOrReel is executed, BACK is pressed surgically to keep the host app alive.
            // Rapid accessibility events (within 800ms) fired during window transitions must NOT count as consecutive failures.
            // Only escalate to ejectToHomeScreen if:
            // 1. It is a dedicated tab (Shorts/Reels bottom tab) where BACK cannot leave the tab, OR
            // 2. BACK has been pressed at least 3 distinct times (>800ms apart) and the player still refuses to dismiss.
            if (now - lastBlockTimestamp in 800L..3500L && targetPkg == lastBlockedPkg) {
                consecutiveBlockCount++
            } else if (now - lastBlockTimestamp > 3500L) {
                consecutiveBlockCount = 1
            }
            lastBlockTimestamp = now
            lastBlockedPkg = targetPkg

            // 1. PHYSICAL HAPTIC SHOCK
            triggerHapticAlert()

            // 2. SURGICAL SHORT CLOSE OR INSTANT HOME EJECTION
            withContext(Dispatchers.Main) {
                val isWholeAppCategory = result.category == ContentCategory.ADULT_WEBSITE || 
                                         result.category == ContentCategory.ADULT_KEYWORD ||
                                         result.category == ContentCategory.TIKTOK

                if (isWholeAppCategory) {
                    // Adult sites or pure short-video apps (TikTok): eject to Home immediately
                    ejectToHomeScreen()
                    val feedbackText = if (result.category == ContentCategory.TIKTOK) {
                        "🎵 TIKTOK BLOCKED: Feed closed."
                    } else {
                        "🛡️ Explicit content blocked. Exiting to Home."
                    }
                    Toast.makeText(applicationContext, feedbackText, Toast.LENGTH_SHORT).show()
                } else {
                    // Dedicated bottom navigation tabs trap the user; BACK cannot exit the tab, so eject to Home.
                    // For feeds/search shorts, surgical BACK closes the overlay, keeping the host app alive!
                    // Eject to Home only after 3 consecutive failed dismissals.
                    if (isDedicatedTab || consecutiveBlockCount >= 3) {
                        ejectToHomeScreen()
                    } else {
                        closeActiveShortsOrReel()
                    }

                    val isBedtime = habitConfig.bedtimeShieldEnabled && habitConfig.isBedtimeActive(now)
                    val feedbackText = when {
                        isNuclear -> "☢️ NUCLEAR SHIELD: Reel/Short closed."
                        lockState.isCurrentlyActive(now) -> "🔒 FOCUS LOCK: Reel/Short closed."
                        isBedtime -> "🌙 BEDTIME SHIELD: Sleep is your superpower. Put your phone down!"
                        else -> "🛡️ SURGICAL SHIELD: Reel/Short closed."
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
        } else {
            // Clean non-blocked screen: reset consecutive block counter immediately
            consecutiveBlockCount = 0
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
