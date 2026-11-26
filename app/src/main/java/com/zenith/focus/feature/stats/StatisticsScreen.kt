package com.zenith.focus.feature.stats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.MovieFilter
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.repository.DailyStat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    todayTotalBlocks: Int,
    focusStreakDays: Int,
    dailyStats: List<DailyStat>,
    todayShorts: Int,
    todayReels: Int,
    todaySpotlight: Int,
    todayAdult: Int,
    todayTamper: Int = 0,
    todayAppLock: Int = 0,
    todayImpulsesDefeated: Int = 0,
    todayDoomscrollsBroken: Int = 0,
    recentEvents: List<BlockEvent> = emptyList(),
    onExportCsv: suspend () -> String,
    onClearStats: suspend () -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateAppLock: () -> Unit = {},
    onStartLockClicked: () -> Unit = {}
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showClearDialog by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableIntStateOf(6) } // Default to today (last of 7 days)

    // Warm Ivory Linen & Peach Mist Gradient Canvas
    val canvasBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFAF7F2),
                Color(0xFFFBF1E8),
                Color(0xFFF6EFEB)
            )
        )
    }

    // Today's reclaimed focus minutes (consistent with HomeScreen FocusScoreCalculator formula)
    val todayMinutesSaved = remember(todayTotalBlocks, todayImpulsesDefeated, todayDoomscrollsBroken, todayTamper) {
        val standardBlocks = (todayTotalBlocks - todayImpulsesDefeated - todayDoomscrollsBroken - todayTamper).coerceAtLeast(0)
        FocusScoreCalculator.calculateTimeReclaimedMinutes(
            impulsesDefeated = todayImpulsesDefeated,
            doomscrollsBroken = todayDoomscrollsBroken,
            shortFormBlocks = standardBlocks
        )
    }

    val weekMinutesSaved = remember(dailyStats, todayMinutesSaved) {
        if (dailyStats.isNotEmpty()) {
            val pastDays = dailyStats.dropLast(1).sumOf { it.estimatedFocusMinutesSaved }
            pastDays + maxOf(dailyStats.lastOrNull()?.estimatedFocusMinutesSaved ?: 0, todayMinutesSaved)
        } else {
            todayMinutesSaved
        }
    }

    val hoursSaved = todayMinutesSaved / 60
    val minsSaved = todayMinutesSaved % 60
    val weekHoursSaved = weekMinutesSaved / 60
    val weekMinsSaved = weekMinutesSaved % 60
    val weekSummaryFormatted = if (weekHoursSaved > 0) "${weekHoursSaved}h ${weekMinsSaved}m" else "${weekMinsSaved}m"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            // ====================================================================
            // 1. TOP APP BAR: Frosted Squircle Back | Title | Streak Chip
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.90f))
                        .border(1.dp, earth.border, RoundedCornerShape(16.dp))
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = earth.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Attention Insights",
                    color = earth.textPrimary,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(OrbitalVioletSoft)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔥 $focusStreakDays d",
                            color = OrbitalViolet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 2. DOMINANT HERO METRIC: Saved Focus Time
            // ====================================================================
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(28.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Soft diffused aurora glow inside the hero card
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    OrbitalViolet.copy(alpha = 0.12f),
                                    OrbitalCoral.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.85f, size.height * 0.25f),
                                radius = size.minDimension * 0.7f
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "PROTECTED ATTENTION (TODAY)",
                            color = OrbitalViolet,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = if (hoursSaved > 0) "${hoursSaved}h ${minsSaved}m" else "${minsSaved}m",
                                color = earth.textPrimary,
                                fontSize = 38.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "reclaimed • 7d: $weekSummaryFormatted",
                                color = earth.textMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (todayTotalBlocks > 0) {
                                "Deflected $todayTotalBlocks addictive kicks today. Dopamine baseline steady."
                            } else {
                                "Zero distractions opened today. Perfect mindful focus."
                            },
                            color = earth.textMuted,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 3. WEEKLY TREND BAR CHART (PAST 7 DAYS)
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "WEEKLY PATTERN",
                    color = earth.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Past 7 Days",
                    color = earth.textMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Build 7 padded day entries ending today
                    val paddedDaily = remember(dailyStats, todayTotalBlocks, todayMinutesSaved) {
                        val base: MutableList<DailyStat?> = if (dailyStats.isNotEmpty()) {
                            dailyStats.takeLast(7).map { it as DailyStat? }.toMutableList()
                        } else {
                            MutableList(7) { null }
                        }
                        while (base.size < 7) {
                            base.add(0, null)
                        }
                        val todayCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val existingLast = base.last()
                        base[base.lastIndex] = if (existingLast != null) {
                            existingLast.copy(
                                totalBlocks = maxOf(existingLast.totalBlocks, todayTotalBlocks),
                                estimatedFocusMinutesSaved = maxOf(existingLast.estimatedFocusMinutesSaved, todayMinutesSaved)
                            )
                        } else {
                            DailyStat(
                                dayTimestamp = todayCal.timeInMillis,
                                totalBlocks = todayTotalBlocks,
                                shortsBlocks = todayShorts,
                                reelsBlocks = todayReels,
                                spotlightBlocks = todaySpotlight,
                                adultBlocks = todayAdult,
                                estimatedFocusMinutesSaved = todayMinutesSaved
                            )
                        }
                        base
                    }

                    val dayInitials = remember(paddedDaily) {
                        (0..6).map { index ->
                            val ts = paddedDaily.getOrNull(index)?.dayTimestamp ?: Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, -(6 - index))
                            }.timeInMillis
                            SimpleDateFormat("EEE", Locale.getDefault()).format(Date(ts)).take(1)
                        }
                    }

                    val dayNames = remember(paddedDaily) {
                        (0..6).map { index ->
                            if (index == 6) "Today" else {
                                val ts = paddedDaily.getOrNull(index)?.dayTimestamp ?: Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, -(6 - index))
                                }.timeInMillis
                                SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(ts))
                            }
                        }
                    }

                    val counts = remember(paddedDaily) {
                        paddedDaily.map { (it?.totalBlocks ?: 0).toFloat() }
                    }

                    val maxVal = remember(counts) { (counts.maxOrNull() ?: 8f).coerceAtLeast(8f) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        counts.forEachIndexed { index, value ->
                            val isSelected = index == selectedDayIndex
                            val barHeightFraction = if (value <= 0f) 0.05f else (value / maxVal).coerceIn(0.14f, 1f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedDayIndex = index }
                            ) {
                                if (isSelected) {
                                    Text(
                                        text = value.toInt().toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OrbitalViolet
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height((100 * barHeightFraction).dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) {
                                                Brush.verticalGradient(
                                                    colors = listOf(OrbitalViolet, OrbitalCoral)
                                                )
                                            } else {
                                                Brush.verticalGradient(
                                                    colors = listOf(Color(0xFFE4DFD6), Color(0xFFDDD6CB))
                                                )
                                            }
                                        )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = dayInitials.getOrElse(index) { "•" },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) earth.textPrimary else earth.textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val selStat = paddedDaily.getOrNull(selectedDayIndex)
                    val selDayTitle = dayNames.getOrElse(selectedDayIndex) { "Day" }
                    val selBlocks = selStat?.totalBlocks ?: 0
                    val selMins = selStat?.estimatedFocusMinutesSaved ?: 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrbitalVioletSoft.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (selBlocks > 0) {
                                "$selDayTitle • $selBlocks blocks (${selMins}m saved)"
                            } else {
                                "$selDayTitle • 0 distractions intercepted"
                            },
                            color = OrbitalViolet,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 4. CATEGORY BREAKDOWN: 2x2 BENTO GRID
            // ====================================================================
            Text(
                text = "INTERCEPTION SOURCES",
                color = earth.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento 1: YouTube Shorts
                StatsBentoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.PlayCircleOutline,
                    iconTint = OrbitalCoral,
                    title = "YouTube Shorts",
                    count = todayShorts
                )

                // Bento 2: Reels & Short Video
                StatsBentoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.MovieFilter,
                    iconTint = OrbitalViolet,
                    title = "Reels & Video",
                    count = todayReels
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento 3: Adult Barrier
                StatsBentoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Shield,
                    iconTint = OrbitalSky,
                    title = "Adult Web",
                    count = todayAdult
                )

                // Bento 4: App Lock, Spotlight, Tamper, Impulses & Doomscroll Shields
                StatsBentoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Apps,
                    iconTint = OrbitalAmber,
                    title = "App & Feed Shields",
                    count = todayAppLock + todaySpotlight + todayTamper + todayImpulsesDefeated + todayDoomscrollsBroken
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 5. RECENT ACTIVITY STREAM (AUDIT LOG)
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "RECENT DEFLECTIONS",
                    color = earth.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${recentEvents.take(15).size} events",
                    color = earth.textMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, earth.border, RoundedCornerShape(22.dp))
            ) {
                if (recentEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🛡️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Zero deflections recorded yet today",
                                color = earth.textPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Addictive feeds and adult sites will appear here when deflected.",
                                color = earth.textMuted,
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        recentEvents.take(8).forEachIndexed { idx, event ->
                            if (idx > 0) {
                                Divider(color = Color(0xFFF0EBE1), thickness = 1.dp)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.category.displayName,
                                        color = earth.textPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = event.packageName.ifBlank { "Immediate hardware kick" },
                                        color = earth.textMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                val diffSeconds = (System.currentTimeMillis() - event.timestamp) / 1000L
                                val relativeTimeStr = when {
                                    diffSeconds < 60 -> "Just now"
                                    diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
                                    diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
                                    else -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.timestamp))
                                }

                                Text(
                                    text = relativeTimeStr,
                                    color = earth.textMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================================================================
            // 6. ACTION CONTROLS: EXPORT AUDIT CSV & CLEAR HISTORY
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Export CSV Button
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val csv = onExportCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            if (clipboard != null) {
                                clipboard.setPrimaryClip(ClipData.newPlainText("Reel Narcotics Audit CSV", csv))
                                Toast.makeText(context, "Audit CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard service unavailable on this device", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Export",
                        tint = earth.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export CSV",
                        color = earth.textPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Clear History Button
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Clear",
                        tint = OrbitalCoral,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clear Data",
                        color = OrbitalCoral,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Reset Insights Data?",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            },
            text = {
                Text(
                    text = "This will erase past interception events and reset the statistics ledger. This action cannot be undone.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            onClearStats()
                            showClearDialog = false
                            Toast.makeText(context, "Statistics reset.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrbitalCoral),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun StatsBentoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    count: Int
) {
    val earth = EarthTheme.colors
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
        modifier = modifier.border(1.dp, earth.border, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = count.toString(),
                color = earth.textPrimary,
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = earth.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
