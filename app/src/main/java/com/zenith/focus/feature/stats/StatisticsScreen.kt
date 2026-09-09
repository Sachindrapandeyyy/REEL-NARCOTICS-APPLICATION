package com.zenith.focus.feature.stats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.EarthBorderLinen
import com.zenith.focus.core.designsystem.EarthCamelOchre
import com.zenith.focus.core.designsystem.EarthCanvasCream
import com.zenith.focus.core.designsystem.EarthForestDark
import com.zenith.focus.core.designsystem.EarthForestGreen
import com.zenith.focus.core.designsystem.EarthSageOlive
import com.zenith.focus.core.designsystem.EarthSurfaceLinen
import com.zenith.focus.core.designsystem.EarthSurfaceLinenSoft
import com.zenith.focus.core.designsystem.EarthTextDark
import com.zenith.focus.core.designsystem.EarthTextMuted
import com.zenith.focus.domain.model.BlockEvent
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.domain.repository.DailyStat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
    recentEvents: List<BlockEvent> = emptyList(),
    onExportCsv: suspend () -> String,
    onClearStats: suspend () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EarthCanvasCream)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // TOP HEADER
        Text(
            text = "AUDIT TRAIL & METRICS",
            color = EarthForestGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Focus Intelligence",
            color = EarthForestDark,
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "Verified SQLite event ledger. 100% on-device, zero estimation, zero telemetry.",
            color = EarthTextMuted,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        // KEY METRICS ROW (TOTAL KICKS & STREAK)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Today's Kicks
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL KICKS TODAY",
                        color = EarthTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$todayTotalBlocks",
                        color = EarthForestDark,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Distractions Blocked",
                        color = EarthCamelOchre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Card 2: Streak
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DISCIPLINE STREAK",
                        color = EarthTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (focusStreakDays > 0) "$focusStreakDays Days" else "0 Days",
                        color = EarthForestDark,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = if (focusStreakDays > 1) "Active Streak" else "Protection Active",
                        color = EarthForestGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7-DAY VISUAL ACTIVITY BAR CHART
        Text(
            text = "7-DAY INTERCEPTION HISTORY",
            color = EarthForestGreen,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                WeeklyActivityChart(
                    stats = dailyStats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("7 Days Ago", color = EarthTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Today ($todayTotalBlocks kicks)",
                        color = EarthCamelOchre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PLATFORM BREAKDOWN SECTION
        Text(
            text = "TODAY'S KICKS BY TARGET",
            color = EarthForestGreen,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        PlatformBreakdownRow("YouTube Shorts", todayShorts, EarthForestGreen)
        PlatformBreakdownRow("Instagram Reels", todayReels, EarthCamelOchre)
        PlatformBreakdownRow("Facebook Reels", 0, EarthSageOlive)
        PlatformBreakdownRow("Adult & Explicit Websites", todayAdult, EarthForestDark)
        if (todayTamper > 0) {
            PlatformBreakdownRow("Anti-Tamper Lockouts", todayTamper, EarthCamelOchre)
        }
        if (todaySpotlight > 0) {
            PlatformBreakdownRow("Snapchat Spotlight", todaySpotlight, EarthSageOlive)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LIVE INTERCEPTION AUDIT FEED
        Text(
            text = "RECENT INTERCEPTIONS AUDIT LOG",
            color = EarthForestGreen,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (recentEvents.isEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛡️ Pure Focus Maintained",
                        color = EarthForestDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No addictive feeds or tamper attempts intercepted yet today.",
                        color = EarthTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    recentEvents.take(15).forEachIndexed { index, event ->
                        val timeStr = remember(event.timestamp) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.timestamp))
                        }
                        val categoryName = event.category.displayName
                        val isTamper = event.category == ContentCategory.SYSTEM_TAMPER

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = categoryName,
                                    color = EarthTextDark,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$timeStr • Auto-Ejected",
                                    color = EarthTextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isTamper) EarthCamelOchre.copy(alpha = 0.15f) else EarthForestGreen.copy(alpha = 0.15f))
                                    .border(
                                        1.dp,
                                        if (isTamper) EarthCamelOchre.copy(alpha = 0.4f) else EarthForestGreen.copy(alpha = 0.35f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isTamper) "LOCKED" else "KICKED",
                                    color = if (isTamper) EarthCamelOchre else EarthForestGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        if (index < recentEvents.take(15).size - 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LOCAL DATA EXPORT & CLEAR ACTIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val csv = onExportCsv()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Reel Narcotics Statistics", csv)
                        clipboard.setPrimaryClip(clip)
                        val rowCount = csv.lineSequence().filter { it.isNotBlank() }.count() - 1
                        Toast.makeText(
                            context,
                            "Exported $rowCount interception records to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EarthForestGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Export CSV", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        onClearStats()
                        Toast.makeText(context, "Statistics reset", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EarthSurfaceLinen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear History", color = EarthTextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun PlatformBreakdownRow(name: String, count: Int, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, EarthBorderLinen, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = name, color = EarthTextDark, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "$count kicks",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeeklyActivityChart(
    stats: List<DailyStat>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val count = maxOf(7, stats.size)
        val barWidth = (w / count) * 0.55f
        val gap = (w / count)

        val maxVal = stats.maxOfOrNull { it.totalBlocks }?.coerceAtLeast(10) ?: 10

        stats.takeLast(7).forEachIndexed { index, stat ->
            val fraction = (stat.totalBlocks.toFloat() / maxVal.toFloat()).coerceIn(0.08f, 1f)
            val barHeight = (h - 16.dp.toPx()) * fraction
            val x = index * gap + (gap - barWidth) / 2f
            val y = h - barHeight

            val barColor = if (index == stats.size - 1) Color(0xFFBC9259) else Color(0xFF204844)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}


