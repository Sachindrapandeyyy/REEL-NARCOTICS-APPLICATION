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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.toArgb
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
import com.zenith.focus.feature.nuclear.NuclearExtendDialog
import com.zenith.focus.feature.onboarding.OnboardingScreen
import com.zenith.focus.feature.protection.ProtectionScreen
import com.zenith.focus.feature.settings.SettingsScreen
import com.zenith.focus.feature.stats.StatisticsScreen
import com.zenith.focus.feature.unlock.UnlockFrictionDialog
import com.zenith.focus.core.update.UpdateState
import com.zenith.focus.core.update.ui.UpdateDialog
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
        val appLockRepo = container.appLockRepository
        val updateManager = container.updateManager

        checkAccessibilityStatus()

        setContent {
            val appTheme by settingsRepo.appTheme.collectAsState()
            val isDarkTheme = when (appTheme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            ZenithFocusTheme(darkTheme = isDarkTheme) {
                val view = androidx.compose.ui.platform.LocalView.current
                val earthColors = com.zenith.focus.core.designsystem.EarthTheme.colors
                if (!view.isInEditMode) {
                    androidx.compose.runtime.SideEffect {
                        val window = (view.context as android.app.Activity).window
                        window.statusBarColor = earthColors.canvas.toArgb()
                        window.navigationBarColor = earthColors.canvas.toArgb()
                        val insets = androidx.core.view.WindowCompat.getInsetsController(window, view)
                        insets.isAppearanceLightStatusBars = true
                        insets.isAppearanceLightNavigationBars = true
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isOnboardingDone by settingsRepo.isOnboardingCompleted.collectAsState()
                    val lockState by lockRepo.lockState.collectAsState()
                    val nuclearSession by nuclearRepo.session.collectAsState()
                    val config by settingsRepo.protectionConfig.collectAsState()
                    val habitConfig by settingsRepo.habitConfig.collectAsState()
                    val appLockConfig by appLockRepo.appLockConfig.collectAsState()
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
                    var showNuclearExtendDialog by remember { mutableStateOf(false) }
                    var showAppLockScreen by remember { mutableStateOf(false) }
                    var showAppPickerDialog by remember { mutableStateOf(false) }
                    var showGuideDialog by remember { mutableStateOf(false) }
                    var pickerInitialMode by remember { mutableStateOf(com.zenith.focus.domain.model.AppLockMode.NUCLEAR_ONLY) }
                    val updateState by updateManager.state.collectAsState()
                    var showGlobalUpdateDialog by remember { mutableStateOf(false) }
                    var userTriggeredUpdateCheck by remember { mutableStateOf(false) }
                    var canInstallPackages by remember { mutableStateOf(updateManager.canRequestPackageInstalls()) }
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

                    // Background tick for lock expiration and stats
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        while (true) {
                            refreshStats()
                            nuclearRepo.checkAndUpdateExpiration()
                            lockRepo.refreshLockState()
                            isDeviceAdminActive = ZenithDeviceAdminReceiver.isAdminActive(context)
                            canInstallPackages = updateManager.canRequestPackageInstalls()
                            delay(1000L)
                        }
                    }

                    // Automatic OTA update check on launch (with 1.5s delay to let UI settle)
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        delay(1500L)
                        updateManager.checkForUpdates()
                    }

                    // Automatically trigger global update modal when actionable update state arrives
                    androidx.compose.runtime.LaunchedEffect(updateState) {
                        if (updateState is UpdateState.Available ||
                            updateState is UpdateState.Downloading ||
                            updateState is UpdateState.Verifying ||
                            updateState is UpdateState.ReadyToInstall ||
                            updateState is UpdateState.Failed
                        ) {
                            showGlobalUpdateDialog = true
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
                        // Global system BackHandler for proper back-stack navigation
                        androidx.activity.compose.BackHandler(enabled = true) {
                            when {
                                showAppPickerDialog -> showAppPickerDialog = false
                                showAppLockScreen -> showAppLockScreen = false
                                showGuideDialog -> showGuideDialog = false
                                showNuclearArmingDialog -> showNuclearArmingDialog = false
                                showNuclearExtendDialog -> showNuclearExtendDialog = false
                                showLockDialog -> showLockDialog = false
                                showUnlockDialog -> showUnlockDialog = false
                                showGlobalUpdateDialog -> showGlobalUpdateDialog = false
                                selectedTab != 0 -> selectedTab = 0
                                else -> finish()
                            }
                        }

                        Scaffold(
                            bottomBar = {
                                ZenithBottomNavigation(
                                    selectedTab = selectedTab,
                                    onTabSelected = { tab ->
                                        showAppLockScreen = false
                                        showAppPickerDialog = false
                                        showGuideDialog = false
                                        selectedTab = tab
                                    },
                                    onFabClicked = { showLockDialog = true }
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
                                        isDarkTheme = isDarkTheme,
                                        onToggleTheme = {
                                            coroutineScope.launch {
                                                settingsRepo.setAppTheme(if (isDarkTheme) "LIGHT" else "DARK")
                                            }
                                        },
                                        onArmNuclearClicked = {
                                            if (nuclearSession.isCurrentlyActive()) {
                                                showNuclearExtendDialog = true
                                            } else {
                                                showNuclearArmingDialog = true
                                            }
                                        },
                                        onStartLockClicked = {
                                            if (nuclearSession.isCurrentlyActive()) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "☢️ Nuclear Mode is active! Feeds are already locked unconditionally.",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                showLockDialog = true
                                            }
                                        },
                                        onUnlockClicked = { showUnlockDialog = true },
                                        onNavigateProtection = { selectedTab = 1 },
                                        onNavigateStats = { selectedTab = 2 },
                                        onEnableAccessibility = {
                                            com.zenith.focus.core.permission.OemNavigationManager.openAccessibilitySettings(this@MainActivity)
                                        },
                                        onEnableDeviceAdmin = {
                                            ZenithDeviceAdminReceiver.openDeviceAdminActivation(context)
                                        },
                                        onOpenGuide = { showGuideDialog = true },
                                        onNavigateAppLock = { showAppLockScreen = true },
                                        lockedAppsCount = appLockConfig.totalCount
                                    )
                                    1 -> ProtectionScreen(
                                        nuclearSession = nuclearSession,
                                        config = config,
                                        appLockConfig = appLockConfig,
                                        onNavigateAppLock = { showAppLockScreen = true },
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
                                        },
                                        onNavigateBack = { selectedTab = 0 },
                                        onNavigateAppLock = { showAppLockScreen = true },
                                        onStartLockClicked = { showLockDialog = true }
                                    )
                                    3 -> SettingsScreen(
                                        config = config,
                                        isServiceConnected = isServiceConnected,
                                        isNuclearActive = nuclearSession.isCurrentlyActive(),
                                        currentTheme = appTheme,
                                        updateManager = updateManager,
                                        habitConfig = habitConfig,
                                        onUpdateHabitConfig = { updated ->
                                            coroutineScope.launch { settingsRepo.updateHabitConfig(updated) }
                                        },
                                        todayTotalBlocks = todayTotal,
                                        focusStreakDays = streakDays,
                                        onShowUpdateDialog = {
                                            userTriggeredUpdateCheck = true
                                            updateManager.checkForUpdates()
                                            showGlobalUpdateDialog = true
                                        },
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
                                                 showUnlockDialog = false
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
                                        nuclearAppsCount = appLockConfig.nuclearCount,
                                        onManageApps = {
                                            showNuclearArmingDialog = false
                                            showAppLockScreen = true
                                        },
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

                                // Nuclear Extend Modal (Active Session -> Add Hours/Days to End Time)
                                if (showNuclearExtendDialog) {
                                    NuclearExtendDialog(
                                        session = nuclearSession,
                                        onDismiss = { showNuclearExtendDialog = false },
                                        onConfirmExtension = { additionalMillis ->
                                            coroutineScope.launch {
                                                val success = nuclearRepo.extendActiveSession(additionalMillis)
                                                if (success) {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "☢️ Nuclear lock extended successfully!",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                showNuclearExtendDialog = false
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

                                // App Lock Shield Dashboard Screen Modal
                                if (showAppLockScreen) {
                                    com.zenith.focus.feature.applock.AppLockScreen(
                                        appLockConfig = appLockConfig,
                                        isNuclearActive = nuclearSession.isCurrentlyActive(),
                                        onBackClicked = { showAppLockScreen = false },
                                        onToggleAppLock = { enabled ->
                                            coroutineScope.launch { appLockRepo.setAppLockEnabled(enabled) }
                                        },
                                        onAddAppsClicked = {
                                            pickerInitialMode = com.zenith.focus.domain.model.AppLockMode.NUCLEAR_ONLY
                                            showAppPickerDialog = true
                                        },
                                        onUpdateMode = { pkg, mode ->
                                            coroutineScope.launch { appLockRepo.updateAppLockMode(pkg, mode) }
                                        },
                                        onUnlockApp = { pkg ->
                                            coroutineScope.launch { appLockRepo.unlockApp(pkg) }
                                        }
                                    )
                                }

                                // App Picker Dialog
                                if (showAppPickerDialog) {
                                    com.zenith.focus.feature.applock.AppPickerDialog(
                                        appLockRepository = appLockRepo,
                                        initialMode = pickerInitialMode,
                                        onDismiss = { showAppPickerDialog = false },
                                        onAppsSelected = { apps, mode ->
                                            coroutineScope.launch {
                                                appLockRepo.lockApps(apps, mode)
                                                showAppPickerDialog = false
                                            }
                                        }
                                    )
                                }

                                // In-App Feature Guide Dialog
                                if (showGuideDialog) {
                                    com.zenith.focus.core.ui.ZenithGuideDialog(
                                        onDismiss = { showGuideDialog = false }
                                    )
                                }

                                // Global In-App OTA Update Dialog
                                if (showGlobalUpdateDialog && updateState !is UpdateState.Idle &&
                                    (userTriggeredUpdateCheck || (updateState !is UpdateState.Checking && updateState !is UpdateState.UpToDate))
                                ) {
                                    UpdateDialog(
                                        state = updateState,
                                        currentVersionName = updateManager.currentVersionName,
                                        onDismiss = {
                                            showGlobalUpdateDialog = false
                                            userTriggeredUpdateCheck = false
                                            updateManager.resetState()
                                        },
                                        onStartDownload = { manifest ->
                                            updateManager.startDownload(manifest)
                                        },
                                        onInstall = { apkFile ->
                                            updateManager.installUpdate(apkFile)
                                        },
                                        onOpenSettings = {
                                            context.startActivity(updateManager.getManageUnknownAppSourcesIntent())
                                        },
                                        canInstallPackages = canInstallPackages,
                                        onRetry = {
                                            updateManager.checkForUpdates()
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
        val app = application as? ZenithApplication ?: return
        val orch = app.container.permissionOrchestrator
        val state = orch.refresh(this)
        ServiceStateBroadcaster.updateConnected(state.isAccessibilityGranted)
    }
}

@Composable
fun ZenithBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onFabClicked: () -> Unit
) {
    val earth = com.zenith.focus.core.designsystem.EarthTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.90f),
            shape = RoundedCornerShape(32.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = earth.border,
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 0: HOME
                BottomNavItem(
                    label = "Home",
                    icon = androidx.compose.material.icons.Icons.Outlined.Home,
                    isSelected = selectedTab == 0,
                    onClick = { onTabSelected(0) }
                )

                // 1: SHIELDS
                BottomNavItem(
                    label = "Shields",
                    icon = androidx.compose.material.icons.Icons.Outlined.Shield,
                    isSelected = selectedTab == 1,
                    onClick = { onTabSelected(1) }
                )

                // CENTER FAB: Quick Lock (+)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(earth.strawberryPink)
                        .clickable { onFabClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = "Quick Lock",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2: INSIGHTS (ANALYTICS)
                BottomNavItem(
                    label = "Insights",
                    icon = androidx.compose.material.icons.Icons.Outlined.BarChart,
                    isSelected = selectedTab == 2,
                    onClick = { onTabSelected(2) }
                )

                // 3: MORE (SETTINGS)
                BottomNavItem(
                    label = "More",
                    icon = androidx.compose.material.icons.Icons.Outlined.MoreHoriz,
                    isSelected = selectedTab == 3,
                    onClick = { onTabSelected(3) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val earth = com.zenith.focus.core.designsystem.EarthTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) earth.surfaceVariant else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = if (isSelected) 12.dp else 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) earth.textPrimary else earth.textMuted,
            modifier = Modifier.size(22.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = earth.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
