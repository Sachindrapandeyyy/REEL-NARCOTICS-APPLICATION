package com.zenith.focus.feature.applock

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.domain.model.AppLockConfig
import com.zenith.focus.domain.model.AppLockMode
import com.zenith.focus.domain.model.LockedAppRule
import com.zenith.focus.feature.protection.EtherealToggle
import java.util.Locale

@Composable
fun AppLockScreen(
    appLockConfig: AppLockConfig,
    isNuclearActive: Boolean,
    onBackClicked: () -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onAddAppsClicked: () -> Unit,
    onUpdateMode: (packageName: String, mode: AppLockMode) -> Unit,
    onUnlockApp: (packageName: String) -> Unit
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Permanent, 2: Nuclear
    var searchQuery by remember { mutableStateOf("") }
    var appToUnlock by remember { mutableStateOf<LockedAppRule?>(null) }

    val lockedList = remember(appLockConfig.lockedApps) {
        appLockConfig.lockedApps.values.sortedBy { it.appName.lowercase(Locale.US) }
    }

    val filteredList = remember(lockedList, selectedTab, searchQuery) {
        lockedList.filter { rule ->
            val matchesTab = when (selectedTab) {
                1 -> rule.lockMode == AppLockMode.PERMANENT || rule.lockMode == AppLockMode.BOTH
                2 -> rule.lockMode == AppLockMode.NUCLEAR_ONLY || rule.lockMode == AppLockMode.BOTH
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                rule.appName.contains(searchQuery, ignoreCase = true) ||
                rule.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchesTab && matchesSearch
        }
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            // ====================================================================
            // 1. TOP BAR: Squircle Back | Title | Master Toggle
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.90f))
                            .border(1.dp, earth.border, RoundedCornerShape(16.dp))
                            .clickable { onBackClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = earth.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "APPLICATION SHIELD",
                            color = OrbitalMint,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "App Lock",
                            color = earth.textPrimary,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Master Shield Toggle
                EtherealToggle(
                    checked = if (isNuclearActive) true else appLockConfig.isAppLockEnabled,
                    enabled = !isNuclearActive,
                    activeColor = OrbitalMint,
                    onCheckedChange = {
                        if (isNuclearActive) {
                            Toast.makeText(context, "☢️ Master App Lock is locked ON during Nuclear Mode!", Toast.LENGTH_SHORT).show()
                        } else {
                            onToggleAppLock(it)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Nuclear Active Tamper Warning
            if (isNuclearActive) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrbitalCoral.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "☢️", fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                        Column {
                            Text(
                                text = "NUCLEAR LOCK ACTIVE",
                                color = OrbitalCoral,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Locked apps cannot be modified or unlocked until your session expires.",
                                color = earth.textPrimary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ====================================================================
            // 2. 3-METRIC SUMMARY CARDS
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    title = "Total Locked",
                    count = appLockConfig.totalCount,
                    accentColor = OrbitalMint,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Permanent",
                    count = appLockConfig.permanentCount,
                    accentColor = OrbitalAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "Nuclear",
                    count = appLockConfig.nuclearCount,
                    accentColor = OrbitalViolet,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ====================================================================
            // 3. FILTER TABS (FROSTED CHIPS)
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All (${appLockConfig.totalCount})",
                    "Permanent (${appLockConfig.permanentCount})",
                    "Nuclear (${appLockConfig.nuclearCount})"
                ).forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.50f))
                            .border(
                                1.dp,
                                if (isSelected) earth.textPrimary else earth.border,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = earth.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ====================================================================
            // 4. SEARCH BAR
            // ====================================================================
            if (lockedList.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search locked apps...", fontSize = 12.5.sp, color = earth.textMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = earth.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = earth.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.90f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.80f),
                        focusedBorderColor = earth.textPrimary,
                        unfocusedBorderColor = earth.border,
                        focusedTextColor = earth.textPrimary,
                        unfocusedTextColor = earth.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ====================================================================
            // 5. LOCKED APPS LIST OR EMPTY STATE
            // ====================================================================
            if (lockedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.90f))
                                .border(1.dp, earth.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = OrbitalMint,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Apps Locked Yet",
                            color = earth.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Lock distracting social media, games, or video apps permanently or during Nuclear sessions.",
                            color = earth.textMuted,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onAddAppsClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitalMint),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("+ Add Apps to Lock", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No apps match your search or filter",
                        color = earth.textMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.packageName }) { rule ->
                        LockedAppCard(
                            rule = rule,
                            isNuclearActive = isNuclearActive,
                            onUpdateMode = { newMode -> onUpdateMode(rule.packageName, newMode) },
                            onUnlock = {
                                if (isNuclearActive) {
                                    Toast.makeText(
                                        context,
                                        "🔒 Cannot unlock ${rule.appName} while Nuclear Mode is active!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    appToUnlock = rule
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // ====================================================================
            // 6. BOTTOM ACTION BAR: + Lock More Apps
            // ====================================================================
            if (lockedList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onAddAppsClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitalCoral),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock More Apps", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }
        }

        // Confirmation Dialog
        if (appToUnlock != null) {
            val rule = appToUnlock!!
            val isPermanent = rule.lockMode == AppLockMode.PERMANENT || rule.lockMode == AppLockMode.BOTH
            AlertDialog(
                onDismissRequest = { appToUnlock = null },
                containerColor = Color.White,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = "Unlock ${rule.appName}?",
                        color = earth.textPrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove protection for ${rule.appName}? " +
                                if (isPermanent) "This app is currently locked continuously 24/7."
                                else "This app is set to lock during Nuclear mode sessions.",
                        color = earth.textMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val pkg = rule.packageName
                            appToUnlock = null
                            onUnlockApp(pkg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitalCoral),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Remove Lock", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { appToUnlock = null },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Keep Locked", color = earth.textPrimary, fontSize = 12.5.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val earth = EarthTheme.colors
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
        modifier = modifier.border(1.dp, earth.border, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                color = accentColor,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = earth.textMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LockedAppCard(
    rule: LockedAppRule,
    isNuclearActive: Boolean,
    onUpdateMode: (AppLockMode) -> Unit,
    onUnlock: () -> Unit
) {
    val earth = EarthTheme.colors

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, earth.border, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName = rule.packageName, size = 42.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.appName,
                    color = earth.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = rule.packageName,
                    color = earth.textMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Mode Selector Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isPermanent = rule.lockMode == AppLockMode.PERMANENT || rule.lockMode == AppLockMode.BOTH
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPermanent) OrbitalAmber.copy(alpha = 0.15f) else OrbitalViolet.copy(alpha = 0.15f))
                            .clickable(enabled = !isNuclearActive) {
                                val nextMode = if (isPermanent) AppLockMode.NUCLEAR_ONLY else AppLockMode.PERMANENT
                                onUpdateMode(nextMode)
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isPermanent) "🔒 Permanent 24/7" else "☢️ Nuclear Only",
                            color = if (isPermanent) OrbitalAmber else OrbitalViolet,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Unlock / Trash Button
            IconButton(
                onClick = onUnlock,
                enabled = !isNuclearActive
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Unlock",
                    tint = if (isNuclearActive) earth.textMuted.copy(alpha = 0.5f) else OrbitalCoral,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
