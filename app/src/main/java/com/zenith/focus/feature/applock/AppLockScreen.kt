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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith.focus.core.designsystem.EarthTheme
import com.zenith.focus.domain.model.AppLockConfig
import com.zenith.focus.domain.model.AppLockMode
import com.zenith.focus.domain.model.LockedAppRule
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppsClicked,
                containerColor = earth.forestGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lock Apps", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(earth.canvas)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = earth.forestDark
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "⚡ APPS & GAMES SHIELD",
                            color = earth.forestGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "App Lock Shield",
                            color = earth.forestDark,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Master Shield Switch
                Switch(
                    checked = appLockConfig.isAppLockEnabled,
                    onCheckedChange = { onToggleAppLock(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = earth.forestGreen,
                        uncheckedThumbColor = earth.textMuted,
                        uncheckedTrackColor = earth.surface
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nuclear Active Tamper Warning
            if (isNuclearActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(earth.surfaceSoft)
                        .border(1.dp, earth.camelOchre, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "☢️",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "NUCLEAR LOCK ACTIVE",
                                color = earth.camelOchre,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Nuclear-locked apps cannot be modified or unlocked until your session expires.",
                                color = earth.textPrimary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Metrics Summary Row (Opal / AppBlock style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricSummaryCard(
                    title = "Total Locked",
                    count = appLockConfig.totalCount,
                    accentColor = earth.forestGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "🔒 Permanent",
                    count = appLockConfig.permanentCount,
                    accentColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryCard(
                    title = "☢️ Nuclear",
                    count = appLockConfig.nuclearCount,
                    accentColor = earth.camelOchre,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs Row
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) earth.forestGreen else earth.surfaceSoft)
                            .border(
                                1.dp,
                                if (isSelected) earth.forestGreen else earth.border,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else earth.textPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
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
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = earth.surfaceSoft,
                        unfocusedContainerColor = earth.surfaceSoft,
                        focusedBorderColor = earth.forestGreen,
                        unfocusedBorderColor = earth.border,
                        focusedTextColor = earth.textPrimary,
                        unfocusedTextColor = earth.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Locked Apps List or Empty State
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
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(earth.surfaceSoft)
                                .border(1.dp, earth.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = earth.forestGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Apps Locked Yet",
                            color = earth.forestDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Take back your time by locking addictive social media, games, or shopping apps permanently or strictly during Nuclear Mode sessions.",
                            color = earth.textMuted,
                            fontSize = 12.5.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onAddAppsClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("+ Add Apps to Lock", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
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
                                if (isNuclearActive && (rule.lockMode == AppLockMode.NUCLEAR_ONLY || rule.lockMode == AppLockMode.BOTH)) {
                                    Toast.makeText(
                                        context,
                                        "🔒 Cannot unlock ${rule.appName} while Nuclear Mode is active!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    onUnlockApp(rule.packageName)
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
        modifier = modifier.border(1.dp, earth.border, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                color = accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
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
    val isLockedInNuclear = isNuclearActive && (rule.lockMode == AppLockMode.NUCLEAR_ONLY || rule.lockMode == AppLockMode.BOTH)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = earth.surfaceSoft),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, earth.border, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName = rule.packageName, size = 44.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.appName,
                    color = earth.textPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
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

                // Mode Switcher Pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Nuclear Pill
                    val isNuclear = rule.lockMode == AppLockMode.NUCLEAR_ONLY
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isNuclear) earth.camelOchre else earth.surface)
                            .border(1.dp, if (isNuclear) earth.camelOchre else earth.border, RoundedCornerShape(6.dp))
                            .clickable(enabled = !isLockedInNuclear) {
                                onUpdateMode(AppLockMode.NUCLEAR_ONLY)
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "☢️ Nuclear",
                            color = if (isNuclear) Color.White else earth.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = if (isNuclear) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Permanent Pill
                    val isPermanent = rule.lockMode == AppLockMode.PERMANENT || rule.lockMode == AppLockMode.BOTH
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPermanent) Color(0xFFD97706) else earth.surface)
                            .border(1.dp, if (isPermanent) Color(0xFFD97706) else earth.border, RoundedCornerShape(6.dp))
                            .clickable(enabled = !isLockedInNuclear) {
                                onUpdateMode(AppLockMode.PERMANENT)
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🔒 Permanent",
                            color = if (isPermanent) Color.White else earth.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = if (isPermanent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Unlock / Remove Action
            IconButton(
                onClick = onUnlock,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isLockedInNuclear) earth.surface else earth.surfaceSoft)
            ) {
                Icon(
                    imageVector = if (isLockedInNuclear) Icons.Default.Lock else Icons.Default.Delete,
                    contentDescription = "Unlock",
                    tint = if (isLockedInNuclear) earth.camelOchre else earth.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
