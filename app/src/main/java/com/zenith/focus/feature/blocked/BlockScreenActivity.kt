package com.zenith.focus.feature.blocked

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.zenith.focus.accessibility.overlay.BlockOverlayContent
import com.zenith.focus.domain.model.ContentCategory

class BlockScreenActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_REMAINING_MILLIS = "extra_remaining_millis"
        const val EXTRA_REASON = "extra_reason"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val catStr = intent.getStringExtra(EXTRA_CATEGORY) ?: ContentCategory.YOUTUBE_SHORTS.name
        val category = runCatching { ContentCategory.valueOf(catStr) }.getOrDefault(ContentCategory.YOUTUBE_SHORTS)
        val remainingMillis = intent.getLongExtra(EXTRA_REMAINING_MILLIS, 3600000L)
        val reason = intent.getStringExtra(EXTRA_REASON) ?: "Content locked"

        window.statusBarColor = android.graphics.Color.parseColor("#FAF7F2")
        window.navigationBarColor = android.graphics.Color.parseColor("#F6EFEB")
        val insets = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insets.isAppearanceLightStatusBars = true
        insets.isAppearanceLightNavigationBars = true

        setContent {
            MaterialTheme {
                BlockOverlayContent(
                    category = category,
                    initialRemainingMillis = remainingMillis,
                    reason = reason,
                    onGoBack = {
                        // Return to Home Screen
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    }
                )
            }
        }
    }
}
