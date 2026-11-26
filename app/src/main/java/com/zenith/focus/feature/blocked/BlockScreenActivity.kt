package com.zenith.focus.feature.blocked

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zenith.focus.accessibility.overlay.BlockOverlayContent
import com.zenith.focus.domain.model.ContentCategory

class BlockScreenActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_REMAINING_MILLIS = "extra_remaining_millis"
        const val EXTRA_REASON = "extra_reason"
    }

    private var categoryState by mutableStateOf(ContentCategory.YOUTUBE_SHORTS)
    private var remainingMillisState by mutableLongStateOf(3600000L)
    private var reasonState by mutableStateOf("Content locked")

    private fun readIntentExtras(intent: Intent?) {
        val catStr = intent?.getStringExtra(EXTRA_CATEGORY) ?: ContentCategory.YOUTUBE_SHORTS.name
        categoryState = runCatching { ContentCategory.valueOf(catStr) }.getOrDefault(ContentCategory.YOUTUBE_SHORTS)
        remainingMillisState = intent?.getLongExtra(EXTRA_REMAINING_MILLIS, 3600000L) ?: 3600000L
        reasonState = intent?.getStringExtra(EXTRA_REASON) ?: "Content locked"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntentExtras(intent)

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.parseColor("#FAF7F2")
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.parseColor("#F6EFEB")
        }
        val insets = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insets.isAppearanceLightStatusBars = true
        insets.isAppearanceLightNavigationBars = true

        val returnHome = {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            runCatching { startActivity(homeIntent) }
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnHome()
            }
        })

        setContent {
            com.zenith.focus.core.designsystem.ZenithFocusTheme {
                BlockOverlayContent(
                    category = categoryState,
                    initialRemainingMillis = remainingMillisState,
                    reason = reasonState,
                    onGoBack = returnHome
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readIntentExtras(intent)
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            com.zenith.focus.accessibility.overlay.OverlayWindowManager.notifyDismissed()
            if (!isFinishing) {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        com.zenith.focus.accessibility.overlay.OverlayWindowManager.notifyDismissed()
    }
}
