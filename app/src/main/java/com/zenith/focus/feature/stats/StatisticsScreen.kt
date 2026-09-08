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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.ZenithBurgundy
import com.zenith.focus.core.designsystem.ZenithBurgundyDeep
import com.zenith.focus.core.designsystem.ZenithEmeraldAccent
import com.zenith.focus.core.designsystem.ZenithNavy
import com.zenith.focus.core.designsystem.ZenithNavyDark
import com.zenith.focus.core.designsystem.ZenithNavyLight
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
            .background(ZenithNavyDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // TOP HEADER
        Text(
            text = "AUDIT TRAIL & METRICS",
            color = ZenithEmeraldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Focus Intelligence",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Verified SQLite event ledger. 100% on-device, zero estimation, zero telemetry.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        // KEY METRICS ROW (TOTAL KICKS & STREAK)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Today's Kicks
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithNavy),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL KICKS TODAY",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$todayTotalBlocks",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Distractions Terminated",
                        color = ZenithEmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Card 2: Streak
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithNavy),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DISCIPLINE STREAK",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (focusStreakDays > 0) "$focusStreakDays Days" else "0 Days",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (focusStreakDays > 1) "Active Streak" else "Protection Active",
                        color = Color(0xFF06B6D4),
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
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ZenithNavy),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
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
                    Text("7 Days Ago", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Today ($todayTotalBlocks kicks)",
                        color = ZenithEmeraldAccent,
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
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        PlatformBreakdownRow("YouTube Shorts", todayShorts, Color(0xFFF43F5E))
        PlatformBreakdownRow("Instagram Reels", todayReels, Color(0xFF8B5CF6))
        PlatformBreakdownRow("Adult & Explicit Websites", todayAdult, Color(0xFFF59E0B))
        if (todayTamper > 0) {
            PlatformBreakdownRow("Anti-Tamper Lockouts", todayTamper, Color(0xFFEF4444))
        }
        if (todaySpotlight > 0) {
            PlatformBreakdownRow("Snapchat Spotlight", todaySpotlight, Color(0xFF06B6D4))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LIVE INTERCEPTION AUDIT FEED
        Text(
            text = "RECENT INTERCEPTIONS AUDIT LOG",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (recentEvents.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🛡️ Pure Focus Maintained",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No addictive feeds or tamper attempts intercepted yet today.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
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
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$timeStr • Auto-Ejected",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isTamper) ZenithBurgundyDeep else Color(0xFF064E3B))
                                    .border(
                                        1.dp,
                                        if (isTamper) ZenithBurgundy else ZenithEmeraldAccent.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isTamper) "LOCKED" else "KICKED",
                                    color = if (isTamper) Color(0xFFFECACA) else ZenithEmeraldAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
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
                colors = ButtonDefaults.buttonColors(containerColor = ZenithNavyLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Export CSV", color = Color(0xFFE2E8F0), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                colors = ButtonDefaults.buttonColors(containerColor = ZenithNavyLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear History", color = Color(0xFFF43F5E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun PlatformBreakdownRow(name: String, count: Int, color: Color) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ZenithNavy),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
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
                Text(text = name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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

            val barColor = if (index == stats.size - 1) Color(0xFF10B981) else Color(0xFF334155)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}


