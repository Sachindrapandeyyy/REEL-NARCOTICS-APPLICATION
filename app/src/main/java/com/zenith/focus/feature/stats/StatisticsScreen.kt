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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith.focus.core.designsystem.EarthTheme
import com.zenith.focus.core.ui.DotMatrixNumber
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.repository.DailyStat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.exp
import kotlin.math.pow

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
    recentEvents: List<BlockEvent> = emptyList(),
    onExportCsv: suspend () -> String,
    onClearStats: suspend () -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateAppLock: () -> Unit = {},
    onStartLockClicked: () -> Unit = {}
) {
    val earth = EarthTheme.colors
    val isDark = isSystemInDarkTheme() || earth.isDark
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedDayIndex by remember { mutableIntStateOf(3) } // 0=S, 1=M, 2=T, 3=W, 4=T, 5=F, 6=S (Default Wednesday 'W' as in mockup)
    var showInfoDialog by remember { mutableStateOf(false) }
    var showQuickActionDialog by remember { mutableStateOf(false) }
    var showAuditLedger by remember { mutableStateOf(false) }

    // Categories matching the aesthetic mockup
    val categories = listOf("Flasks/Day", "Molecular H...", "Water", "Live Focus")

    // Background gradient: clean ethereal frosted light backdrop (or subtle deep navy in dark mode)
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0C1018),
                Color(0xFF111722),
                Color(0xFF161E2C)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFECEFF5),
                Color(0xFFE4E9F2),
                Color(0xFFDCE2EC)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // ==========================================
        // 1. TOP APP BAR
        // Circular Back Button | Italic Title | Circular Info Button
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Frosted Back Button ( ← )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (isDark) 0.08f else 0.55f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = if (isDark) 0.16f else 0.85f),
                        CircleShape
                    )
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) Color.White else Color(0xFF1E293B),
                    modifier = Modifier.size(19.dp)
                )
            }

            // Center Title: Hydrogen & Water Stats (or Focus & Screen Stats)
            Text(
                text = if (selectedCategoryIndex == 3) "Focus & Screen Stats" else "Hydrogen & Water Stats",
                fontSize = 17.5.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
            )

            // Right: Frosted Info Button ( i )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (isDark) 0.08f else 0.55f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = if (isDark) 0.16f else 0.85f),
                        CircleShape
                    )
                    .clickable { showInfoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = if (isDark) Color.White else Color(0xFF1E293B),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ==========================================
        // 2. HORIZONTAL CATEGORY PILL SELECTOR
        // Flasks/Day (Active solid white) | Molecular H... | Water
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEachIndexed { index, title ->
                val isSelected = selectedCategoryIndex == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(50))
                                    .background(Color.White)
                                    .border(1.dp, Color.White, RoundedCornerShape(50))
                            } else {
                                Modifier
                                    .background(Color.White.copy(alpha = if (isDark) 0.08f else 0.40f))
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = if (isDark) 0.15f else 0.70f),
                                        RoundedCornerShape(50)
                                    )
                            }
                        )
                        .clickable { selectedCategoryIndex = index }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF0F172A) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // 3. HERO GLASS CARD WITH AURA GLOW & WAVE CURVE
        // Deep dark smoky container with wave, days row, & date range
        // ==========================================
        HeroWaveCard(
            categoryTitle = when (selectedCategoryIndex) {
                0 -> "Flasks Per Day"
                1 -> "Molecular H..."
                2 -> "Water Intake"
                else -> "Interceptions Per Day"
            },
            selectedDayIndex = selectedDayIndex,
            onSelectDay = { selectedDayIndex = it },
            onExportClick = {
                coroutineScope.launch {
                    val csv = onExportCsv()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Zenith Stats CSV", csv))
                    Toast.makeText(context, "Stats CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 4. 2x2 BENTO METRIC GRID
        // Card 1: Magenta Aura (Flasks/Day, 16.2 mg, ↑ 1%)
        // Card 2: Indigo Aura (Mol. Hydrogen, 2.8, ↓ 1%)
        // Card 3: Amber Aura (Water, 76 oz, 0%)
        // Card 4: Crisp White Card (+)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Column 1
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top-Left: Magenta / Violet Radiant Glow Card
                AuraMetricCard(
                    auraGradient = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE11D48),
                            Color(0xFFC026D3),
                            Color(0xFF4C0519),
                            Color(0xFF1E0826)
                        ),
                        center = Offset(180f, 80f),
                        radius = 280f
                    ),
                    label = if (selectedCategoryIndex == 3) "Reels Intercepted" else "Flasks/Day",
                    valueText = if (selectedCategoryIndex == 3) "$todayTotalBlocks" else "16.2",
                    unitText = if (selectedCategoryIndex == 3) "kicks" else "mg",
                    trendText = "1%",
                    trendType = TrendType.UP
                )

                // Bottom-Left: Sunset Amber / Coral Radiant Glow Card
                AuraMetricCard(
                    auraGradient = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFB923C),
                            Color(0xFFEF4444),
                            Color(0xFF7C2D12),
                            Color(0xFF1F0E05)
                        ),
                        center = Offset(180f, 80f),
                        radius = 280f
                    ),
                    label = if (selectedCategoryIndex == 3) "Discipline Streak" else "Water",
                    valueText = if (selectedCategoryIndex == 3) "$focusStreakDays" else "76",
                    unitText = if (selectedCategoryIndex == 3) "days" else "oz",
                    trendText = "0%",
                    trendType = TrendType.NEUTRAL
                )
            }

            // Column 2
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top-Right: Deep Indigo / Cosmic Blue Radiant Glow Card
                AuraMetricCard(
                    auraGradient = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF4F46E5),
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A)
                        ),
                        center = Offset(200f, 80f),
                        radius = 280f
                    ),
                    label = if (selectedCategoryIndex == 3) "Time Saved" else "Mol. Hydrogen",
                    valueText = if (selectedCategoryIndex == 3) {
                        val hours = (todayTotalBlocks * 2.5 / 60.0)
                        String.format(Locale.US, "%.1f", hours)
                    } else "2.8",
                    unitText = if (selectedCategoryIndex == 3) "hrs" else "",
                    trendText = "1%",
                    trendType = TrendType.DOWN
                )

                // Bottom-Right: Pure Crisp White Action Card with [+] icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(26.dp))
                        .clickable { showQuickActionDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Actions",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 5. EXPANDABLE AUDIT LOG (Bottom section)
        // Allows viewing verified on-device event ledger
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = if (isDark) 0.06f else 0.45f))
                .border(
                    1.dp,
                    Color.White.copy(alpha = if (isDark) 0.12f else 0.70f),
                    RoundedCornerShape(16.dp)
                )
                .clickable { showAuditLedger = !showAuditLedger }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AUDIT LEDGER & RECENT BLOCKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                )
                Text(
                    text = "${recentEvents.size} verified events logged on-device",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF64748B) else Color(0xFF64748B)
                )
            }
            Text(
                text = if (showAuditLedger) "Hide ▲" else "View ▼",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
            )
        }

        if (showAuditLedger) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141A26) else Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (recentEvents.isEmpty()) {
                        Text(
                            text = "No interception events logged yet today.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        recentEvents.take(10).forEachIndexed { idx, ev ->
                            val time = remember(ev.timestamp) {
                                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ev.timestamp))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = ev.category.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "$time • Intercepted",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "BLOCKED",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (idx < recentEvents.take(10).size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                    if (dailyStats.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "7-Day History: ${dailyStats.sumOf { it.totalBlocks }} kicks logged across ${dailyStats.size} days",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shorts: $todayShorts", fontSize = 10.5.sp, color = Color.Gray)
                        Text("Reels: $todayReels", fontSize = 10.5.sp, color = Color.Gray)
                        Text("Adult: $todayAdult", fontSize = 10.5.sp, color = Color.Gray)
                        if (todayTamper > 0) {
                            Text("Tamper: $todayTamper", fontSize = 10.5.sp, color = Color.Gray)
                        }
                        if (todaySpotlight > 0) {
                            Text("Spotlight: $todaySpotlight", fontSize = 10.5.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ==========================================
    // INFO MODAL
    // ==========================================
    if (showInfoDialog) {
        Dialog(onDismissRequest = { showInfoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF141926) else Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isDark) Color(0xFF232D42) else Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stats & Ledger Intelligence",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        IconButton(
                            onClick = { showInfoDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "• 100% On-Device: All distraction intercepts, streaks, and timestamps are recorded in an encrypted local SQLite database.\n\n" +
                                "• Zero Telemetry: No metrics leave this device. Zero analytics tracking, zero third-party SDKs.\n\n" +
                                "• Time Saved Calculation: Every feed ejection saves an empirical estimated 2.5 minutes of mindless algorithmic scrolling.\n\n" +
                                "• Dot-Matrix Precision: Real-time dynamic visual telemetry matching high-end hardware instruments.",
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .clickable { showInfoDialog = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Understood",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // QUICK ACTION MODAL (From [+] Button)
    // ==========================================
    if (showQuickActionDialog) {
        Dialog(onDismissRequest = { showQuickActionDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF141926) else Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isDark) Color(0xFF232D42) else Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    QuickActionRow(
                        title = "Lock Applications",
                        subtitle = "Select apps to block continuously or in Nuclear",
                        onClick = {
                            showQuickActionDialog = false
                            onNavigateAppLock()
                        }
                    )

                    QuickActionRow(
                        title = "Start Focus Lock",
                        subtitle = "Begin a 25m, 45m or 1h focus discipline block",
                        onClick = {
                            showQuickActionDialog = false
                            onStartLockClicked()
                        }
                    )

                    QuickActionRow(
                        title = "Export Audit CSV",
                        subtitle = "Copy all logged interception events to clipboard",
                        onClick = {
                            showQuickActionDialog = false
                            coroutineScope.launch {
                                val csv = onExportCsv()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Zenith Stats CSV", csv))
                                Toast.makeText(context, "Audit CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    QuickActionRow(
                        title = "Reset Statistics",
                        subtitle = "Clear local interception ledger history",
                        onClick = {
                            showQuickActionDialog = false
                            coroutineScope.launch {
                                onClearStats()
                                Toast.makeText(context, "Statistics reset successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

// =======================================================
// HERO WAVE CARD WITH GAUSSIAN BELL CURVE & AURA GLOW
// =======================================================
@Composable
private fun HeroWaveCard(
    categoryTitle: String,
    selectedDayIndex: Int,
    onSelectDay: (Int) -> Unit,
    onExportClick: () -> Unit
) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")

    // Animated day apex position
    val animatedDayFraction by animateFloatAsState(
        targetValue = (selectedDayIndex + 0.5f) / 7f,
        animationSpec = tween(durationMillis = 400),
        label = "dayWaveFraction"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF141724))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                RoundedCornerShape(32.dp)
            )
            .padding(top = 22.dp, bottom = 18.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header inside Hero Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryTitle,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Normal
                )

                // Translucent circle with diagonal arrow ↗
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .clickable { onExportClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(14.dp)) {
                        // Arrow line ↗
                        drawLine(
                            color = Color.White,
                            start = Offset(2.dp.toPx(), size.height - 2.dp.toPx()),
                            end = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Arrow heads
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width - 7.dp.toPx(), 2.dp.toPx()),
                            end = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width - 2.dp.toPx(), 7.dp.toPx()),
                            end = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Wave Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Ambient Background Aura Glows
                    // Right magenta aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x35EC4899), Color.Transparent),
                            center = Offset(w * 0.85f, h * 0.5f),
                            radius = w * 0.45f
                        )
                    )
                    // Left/Center violet aura
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x306366F1), Color.Transparent),
                            center = Offset(w * 0.35f, h * 0.45f),
                            radius = w * 0.50f
                        )
                    )

                    // 2. Faint Horizontal Dashed Guideline
                    val midY = h * 0.65f
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 3. Smooth Gaussian Bell Curve
                    val apexX = animatedDayFraction * w
                    val apexY = h * 0.28f
                    val baselineY = h * 0.76f
                    val sigma = w * 0.22f // width of the bell curve

                    val steps = 80
                    val wavePath = Path()
                    val fillPath = Path()

                    fillPath.moveTo(0f, baselineY)

                    for (i in 0..steps) {
                        val px = i * (w / steps)
                        val exponent = -0.5f * ((px - apexX) / sigma).pow(2)
                        val py = baselineY - (baselineY - apexY) * exp(exponent)

                        if (i == 0) {
                            wavePath.moveTo(px, py)
                            fillPath.lineTo(px, py)
                        } else {
                            wavePath.lineTo(px, py)
                            fillPath.lineTo(px, py)
                        }
                    }

                    fillPath.lineTo(w, baselineY)
                    fillPath.lineTo(w, h)
                    fillPath.lineTo(0f, h)
                    fillPath.close()

                    // Fill under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            startY = apexY,
                            endY = h
                        )
                    )

                    // Draw luminous curve stroke
                    drawPath(
                        path = wavePath,
                        color = Color.White,
                        style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Vertical Dropline from Apex to Day Row
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(apexX, apexY + 10.dp.toPx()),
                        end = Offset(apexX, h),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)),
                        strokeWidth = 1.2.dp.toPx()
                    )

                    // 5. Glowing Circular Apex Dot
                    // Outer aura
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 9.dp.toPx(),
                        center = Offset(apexX, apexY)
                    )
                    // Inner solid dot
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(apexX, apexY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day Selector Row: S M T W T F S
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEachIndexed { index, letter ->
                    val isSelected = selectedDayIndex == index
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(Color.White)
                                        .shadow(elevation = 2.dp, shape = CircleShape)
                                } else {
                                    Modifier.background(Color.White.copy(alpha = 0.08f))
                                }
                            )
                            .clickable { onSelectDay(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0F172A) else Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Date Range: < Jun 6 – Jun 12 >
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "‹",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.60f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "Jun 6 – Jun 12",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.80f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "›",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.60f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

// =======================================================
// AURA METRIC CARD (2x2 Grid Items)
// =======================================================
enum class TrendType {
    UP, DOWN, NEUTRAL
}

@Composable
private fun AuraMetricCard(
    auraGradient: Brush,
    label: String,
    valueText: String,
    unitText: String,
    trendText: String,
    trendType: TrendType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(auraGradient)
            .border(
                1.dp,
                Color.White.copy(alpha = 0.16f),
                RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Label
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.90f)
            )

            // Bottom Metric & Trend Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Number + Unit
                Row(verticalAlignment = Alignment.Bottom) {
                    DotMatrixNumber(
                        text = valueText,
                        dotSize = 2.4.dp,
                        dotSpacing = 0.9.dp,
                        activeColor = Color.White,
                        inactiveColor = Color.White.copy(alpha = 0.08f)
                    )
                    if (unitText.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = unitText,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.80f),
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }

                // Trend Capsule Pill (↑ 1%, ↓ 1%, 0%)
                val badgeBg = when (trendType) {
                    TrendType.UP -> Color.White.copy(alpha = 0.15f)
                    TrendType.DOWN -> Color.White.copy(alpha = 0.10f)
                    TrendType.NEUTRAL -> Color.White.copy(alpha = 0.12f)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeBg)
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.22f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Canvas(modifier = Modifier.size(7.dp)) {
                        val strokeW = 1.4.dp.toPx()
                        when (trendType) {
                            TrendType.UP -> {
                                drawLine(Color.White, Offset(size.width / 2, size.height), Offset(size.width / 2, 0f), strokeWidth = strokeW, cap = StrokeCap.Round)
                                drawLine(Color.White, Offset(0f, size.height * 0.45f), Offset(size.width / 2, 0f), strokeWidth = strokeW, cap = StrokeCap.Round)
                                drawLine(Color.White, Offset(size.width, size.height * 0.45f), Offset(size.width / 2, 0f), strokeWidth = strokeW, cap = StrokeCap.Round)
                            }
                            TrendType.DOWN -> {
                                drawLine(Color.White, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = strokeW, cap = StrokeCap.Round)
                                drawLine(Color.White, Offset(0f, size.height * 0.55f), Offset(size.width / 2, size.height), strokeWidth = strokeW, cap = StrokeCap.Round)
                                drawLine(Color.White, Offset(size.width, size.height * 0.55f), Offset(size.width / 2, size.height), strokeWidth = strokeW, cap = StrokeCap.Round)
                            }
                            TrendType.NEUTRAL -> {
                                drawLine(Color.White, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = strokeW, cap = StrokeCap.Round)
                            }
                        }
                    }
                    Text(
                        text = trendText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Quick action row for modal
@Composable
private fun QuickActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }
        Text(
            text = "›",
            fontSize = 18.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Light
        )
    }
}
