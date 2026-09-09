package com.zenith.focus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.accessibility.service.ServiceStateBroadcaster
import com.zenith.focus.core.designsystem.ZenithEmeraldAccent
import com.zenith.focus.core.designsystem.ZenithFocusTheme
import com.zenith.focus.core.designsystem.ZenithNavy
import com.zenith.focus.core.designsystem.ZenithNavyLight
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.model.LockMode
import com.zenith.focus.domain.nuclear.NuclearSessionStatus
import com.zenith.focus.domain.repository.DailyStat
import com.zenith.focus.feature.home.HomeScreen
import com.zenith.focus.feature.lock.LockSetupDialog
import com.zenith.focus.feature.nuclear.NuclearArmingDialog
import com.zenith.focus.feature.nuclear.NuclearCompleteDialog
import com.zenith.focus.feature.onboarding.OnboardingScreen
import com.zenith.focus.feature.protection.ProtectionScreen
import com.zenith.focus.feature.settings.SettingsScreen
import com.zenith.focus.feature.stats.StatisticsScreen
import com.zenith.focus.feature.unlock.UnlockFrictionDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as ZenithApplication).container
        val lockRepo = container.lockRepository
        val nuclearRepo = container.nuclearModeRepository
        val settingsRepo = container.settingsRepository
        val statsRepo = container.statisticsRepository

        checkAccessibilityStatus()

        setContent {
            val appTheme by settingsRepo.appTheme.collectAsState()
            val isDarkTheme = when (appTheme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            ZenithFocusTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isOnboardingDone by settingsRepo.isOnboardingCompleted.collectAsState()
                    val lockState by lockRepo.lockState.collectAsState()
                    val nuclearSession by nuclearRepo.session.collectAsState()
                    val config by settingsRepo.protectionConfig.collectAsState()
                    val isServiceConnected by ServiceStateBroadcaster.isServiceConnected.collectAsState()

                    val context = LocalContext.current
                    var isDeviceAdminActive by remember {
                        mutableStateOf(ZenithDeviceAdminReceiver.isAdminActive(context))
                    }
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                isDeviceAdminActive = ZenithDeviceAdminReceiver.isAdminActive(context)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val coroutineScope = rememberCoroutineScope()

                    var showLockDialog by remember { mutableStateOf(false) }
                    var showUnlockDialog by remember { mutableStateOf(false) }
                    var showNuclearArmingDialog by remember { mutableStateOf(false) }
                    var selectedTab by remember { mutableIntStateOf(0) } // 0=Home, 1=Protection, 2=Stats, 3=Settings

                    // Today's reactive metrics
                    var todayTotal by remember { mutableIntStateOf(0) }
                    var todayShorts by remember { mutableIntStateOf(0) }
                    var todayReels by remember { mutableIntStateOf(0) }
                    var todayAdult by remember { mutableIntStateOf(0) }
                    var todayTamper by remember { mutableIntStateOf(0) }
                    var streakDays by remember { mutableIntStateOf(0) }
                    var dailyStats by remember { mutableStateOf<List<DailyStat>>(emptyList()) }
                    var recentEvents by remember { mutableStateOf<List<BlockEvent>>(emptyList()) }

                    val refreshStats = {
                        coroutineScope.launch {
                            todayTotal = statsRepo.getTodayBlockCount()
                            todayShorts = statsRepo.getTodayCountByCategory(ContentCategory.YOUTUBE_SHORTS)
                            todayReels = statsRepo.getTodayCountByCategory(ContentCategory.INSTAGRAM_REELS)
                            todayAdult = statsRepo.getTodayCountByCategory(ContentCategory.ADULT_WEBSITE) +
                                         statsRepo.getTodayCountByCategory(ContentCategory.ADULT_KEYWORD)
                            todayTamper = statsRepo.getTodayCountByCategory(ContentCategory.SYSTEM_TAMPER)
                            streakDays = statsRepo.getFocusStreakDays()
                            statsRepo.getRecentEvents(25).collect {
                                recentEvents = it
                            }
                        }
                    }

                    androidx.compose.runtime.LaunchedEffect(selectedTab) {
                        refreshStats()
                        statsRepo.getDailyStats(7).collect {
                            dailyStats = it
                        }
                    }

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        while (true) {
                            refreshStats()
                            nuclearRepo.checkAndUpdateExpiration()
                            lockRepo.refreshLockState()
                            isDeviceAdminActive = ZenithDeviceAdminReceiver.isAdminActive(context)
                            delay(1000L)
                        }
                    }

                    if (!isOnboardingDone) {
                        OnboardingScreen(
                            config = config,
                            isServiceConnected = isServiceConnected,
                            isDeviceAdminActive = isDeviceAdminActive,
                            onToggleCategory = { category, blocked ->
                                coroutineScope.launch {
                                    settingsRepo.updateCategory(category, blocked)
                                }
                            },
                            onEnableDeviceAdmin = {
                                ZenithDeviceAdminReceiver.openDeviceAdminActivation(context)
                            },
                            onCompleteOnboarding = {
                                coroutineScope.launch {
                                    settingsRepo.setOnboardingCompleted(true)
                                }
                            }
                        )
                    } else {
                        Scaffold(
                            bottomBar = {
                                ZenithBottomNavigation(
                                    selectedTab = selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )
                            }
                        ) { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                when (selectedTab) {
                                    0 -> HomeScreen(
                                        nuclearSession = nuclearSession,
                                        lockState = lockState,
                                        config = config,
                                        isServiceConnected = isServiceConnected,
                                        isDeviceAdminActive = isDeviceAdminActive,
                                        todayTotalBlocks = todayTotal,
                                        todayShortsBlocks = todayShorts,
                                        todayReelsBlocks = todayReels,
                                        todayAdultBlocks = todayAdult,
                                        focusStreakDays = streakDays,
                                        onArmNuclearClicked = { showNuclearArmingDialog = true },
                                        onStartLockClicked = { showLockDialog = true },
                                        onUnlockClicked = { showUnlockDialog = true },
                                        onNavigateProtection = { selectedTab = 1 },
                                        onNavigateStats = { selectedTab = 2 },
                                        onEnableAccessibility = {
                                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            startActivity(intent)
                                        },
                                        onEnableDeviceAdmin = {
                                            ZenithDeviceAdminReceiver.openDeviceAdminActivation(context)
                                        }
                                    )
                                    1 -> ProtectionScreen(
                                        nuclearSession = nuclearSession,
                                        config = config,
                                        onToggleCategory = { cat, blocked ->
                                            coroutineScope.launch { settingsRepo.updateCategory(cat, blocked) }
                                        },
                                        onToggleBrowserProtection = { enabled ->
                                            coroutineScope.launch {
                                                settingsRepo.updateProtectionConfig(config.copy(browserProtectionEnabled = enabled))
                                            }
                                        },
                                        onToggleStrictMode = { enabled ->
                                            coroutineScope.launch { settingsRepo.setStrictMode(enabled) }
                                        }
                                    )
                                    2 -> StatisticsScreen(
                                        todayTotalBlocks = todayTotal,
                                        focusStreakDays = streakDays,
                                        dailyStats = dailyStats,
                                        todayShorts = todayShorts,
                                        todayReels = todayReels,
                                        todaySpotlight = (todayTotal - todayShorts - todayReels - todayAdult - todayTamper).coerceAtLeast(0),
                                        todayAdult = todayAdult,
                                        todayTamper = todayTamper,
                                        recentEvents = recentEvents,
                                        onExportCsv = { statsRepo.exportStatisticsCsv() },
                                        onClearStats = {
                                            statsRepo.clearAllStatistics()
                                            refreshStats()
                                        }
                                    )
                                    3 -> SettingsScreen(
                                        config = config,
                                        isServiceConnected = isServiceConnected,
                                        isNuclearActive = nuclearSession.isCurrentlyActive(),
                                        currentTheme = appTheme,
                                        onSelectFrictionType = { friction ->
                                            coroutineScope.launch { settingsRepo.setFrictionType(friction) }
                                        },
                                        onSetPin = { pin ->
                                            settingsRepo.setPin(pin)
                                        },
                                        onClearPin = {
                                            settingsRepo.clearPin()
                                        },
                                        onSetTheme = { theme ->
                                            coroutineScope.launch { settingsRepo.setAppTheme(theme) }
                                        }
                                    )
                                }

                                // Standard Lock Setup Modal
                                if (showLockDialog) {
                                    LockSetupDialog(
                                        onDismiss = { showLockDialog = false },
                                        onStartLock = { duration, mode, label ->
                                            coroutineScope.launch {
                                                lockRepo.startLock(duration, mode, label)
                                            }
                                        },
                                        onStartUntilTomorrow = {
                                            coroutineScope.launch {
                                                val tomorrowTarget = DateTimeUtils.getNextDayBoundary(targetHour = 4)
                                                lockRepo.startLockUntil(tomorrowTarget, LockMode.UNTIL_TOMORROW, "Until 4:00 AM")
                                            }
                                        }
                                    )
                                }

                                // Unlock Friction Modal (Blocked if Nuclear Mode is Active)
                                if (showUnlockDialog) {
                                    UnlockFrictionDialog(
                                        lockState = lockState,
                                        config = config,
                                        isNuclearActive = nuclearSession.isCurrentlyActive(),
                                        onDismiss = { showUnlockDialog = false },
                                        onUnlockConfirmed = {
                                            coroutineScope.launch {
                                                lockRepo.endLock()
                                            }
                                        },
                                        onVerifyPin = { pin ->
                                            settingsRepo.verifyPin(pin)
                                        }
                                    )
                                }

                                // Nuclear Arming Flow Modal (Commitment Warning -> Duration -> 3s Hold to Activate)
                                if (showNuclearArmingDialog) {
                                    NuclearArmingDialog(
                                        onDismiss = {
                                            coroutineScope.launch {
                                                nuclearRepo.cancelArming()
                                                showNuclearArmingDialog = false
                                            }
                                        },
                                        onArmSession = { durationMillis, enabledCategories ->
                                            coroutineScope.launch {
                                                nuclearRepo.armSession(durationMillis, todayTotal, enabledCategories)
                                            }
                                        },
                                        onConfirmActivation = {
                                            coroutineScope.launch {
                                                nuclearRepo.activateArmedSession()
                                                showNuclearArmingDialog = false
                                            }
                                        }
                                    )
                                }

                                // Nuclear Session Complete Celebration Dialog (Naturally Expired)
                                if (nuclearSession.status == NuclearSessionStatus.EXPIRED) {
                                    val blocksDuringSession = (todayTotal - nuclearSession.blockedCountAtStart).coerceAtLeast(0)
                                    NuclearCompleteDialog(
                                        session = nuclearSession,
                                        totalBlocksDuringSession = blocksDuringSession,
                                        onAcknowledge = {
                                            coroutineScope.launch {
                                                nuclearRepo.acknowledgeCompletedSession()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAccessibilityStatus()
    }

    private fun checkAccessibilityStatus() {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return
        val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val isEnabled = enabledServices.any {
            it.resolveInfo?.serviceInfo?.packageName == packageName
        }
        ServiceStateBroadcaster.updateConnected(isEnabled)
    }
}

@Composable
fun ZenithBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf("HOME", "SHIELD", "ANALYTICS", "SETTINGS")

    Surface(
        color = com.zenith.focus.core.designsystem.SpiderStealthBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = com.zenith.focus.core.designsystem.SpiderBorderDark,
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                )
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) com.zenith.focus.core.designsystem.SpiderCardDark else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) com.zenith.focus.core.designsystem.SpiderRedDark.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) com.zenith.focus.core.designsystem.SpiderRedAccent else Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}
