package com.zenith.focus.feature.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith.focus.core.designsystem.EarthTheme
import com.zenith.focus.domain.model.AppLockMode
import com.zenith.focus.domain.model.InstalledAppInfo
import com.zenith.focus.domain.repository.AppLockRepository
import java.util.Locale

@Composable
fun AppPickerDialog(
    appLockRepository: AppLockRepository,
    initialMode: AppLockMode = AppLockMode.NUCLEAR_ONLY,
    onDismiss: () -> Unit,
    onAppsSelected: (apps: List<Pair<String, String>>, mode: AppLockMode) -> Unit
) {
    val earth = EarthTheme.colors

    var selectedMode by remember { mutableStateOf(initialMode) }
    var searchQuery by remember { mutableStateOf("") }
    var filterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Downloaded, 2: System

    var isLoading by remember { mutableStateOf(true) }
    var installedApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    var selectedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        val apps = appLockRepository.getInstalledApps()
        installedApps = apps
        isLoading = false
    }

    val filteredApps = remember(installedApps, searchQuery, filterTab) {
        installedApps.filter { app ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
            }

            val matchesFilter = when (filterTab) {
                1 -> !app.isSystemApp
                2 -> app.isSystemApp
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = earth.canvas
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with title and close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "APP LOCK SHIELD",
                            color = earth.forestGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Select Apps to Lock",
                            color = earth.forestDark,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = earth.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Selector Segmented Chips
                Text(
                    text = "TARGET LOCK MODE FOR SELECTED APPS:",
                    color = earth.textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nuclear Mode Chip
                    val isNuclearSelected = selectedMode == AppLockMode.NUCLEAR_ONLY
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNuclearSelected) earth.surfaceSoft else earth.surface)
                            .border(
                                width = if (isNuclearSelected) 1.5.dp else 1.dp,
                                color = if (isNuclearSelected) earth.camelOchre else earth.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedMode = AppLockMode.NUCLEAR_ONLY }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "☢️ Nuclear Mode",
                                color = if (isNuclearSelected) earth.forestDark else earth.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isNuclearSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "Locked during Nuclear only",
                                color = earth.textMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Permanent Mode Chip
                    val isPermanentSelected = selectedMode == AppLockMode.PERMANENT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPermanentSelected) earth.surfaceSoft else earth.surface)
                            .border(
                                width = if (isPermanentSelected) 1.5.dp else 1.dp,
                                color = if (isPermanentSelected) Color(0xFFD97706) else earth.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedMode = AppLockMode.PERMANENT }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🔒 Permanent 24/7",
                                color = if (isPermanentSelected) earth.forestDark else earth.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isPermanentSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = "Continuous around-the-clock",
                                color = earth.textMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps...", fontSize = 13.sp, color = earth.textMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = earth.textMuted,
                            modifier = Modifier.size(18.dp)
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
                    shape = RoundedCornerShape(14.dp),
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
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips Row & Selection Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "User", "System").forEachIndexed { index, label ->
                            val isSelected = filterTab == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) earth.forestGreen else earth.surface)
                                    .border(1.dp, if (isSelected) earth.forestGreen else earth.border, RoundedCornerShape(8.dp))
                                    .clickable { filterTab = index }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else earth.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Select All / Deselect All Action
                    Text(
                        text = if (selectedPackages.size == filteredApps.size && filteredApps.isNotEmpty()) "Deselect All" else "Select All (${filteredApps.size})",
                        color = earth.forestGreen,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                if (selectedPackages.size == filteredApps.size) {
                                    selectedPackages = emptySet()
                                } else {
                                    selectedPackages = filteredApps.map { it.packageName }.toSet()
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // App List or Loading Indicator
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = earth.forestGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Scanning installed applications...",
                                color = earth.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No apps found matching '$searchQuery'" else "No apps found",
                            color = earth.textMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isChecked = selectedPackages.contains(app.packageName)
                            val isAlreadyLocked = app.isLocked

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isChecked) earth.surfaceSoft else earth.surface)
                                    .border(
                                        width = if (isChecked) 1.dp else 0.5.dp,
                                        color = if (isChecked) earth.forestGreen else earth.border,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedPackages = if (isChecked) {
                                            selectedPackages - app.packageName
                                        } else {
                                            selectedPackages + app.packageName
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedPackages = if (checked) {
                                            selectedPackages + app.packageName
                                        } else {
                                            selectedPackages - app.packageName
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = earth.forestGreen,
                                        uncheckedColor = earth.textMuted,
                                        checkmarkColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                AppIcon(packageName = app.packageName, size = 40.dp)

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        color = earth.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = earth.textMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isAlreadyLocked && app.lockMode != null) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (app.lockMode == AppLockMode.PERMANENT) Color(0xFF451A03) else earth.surfaceSoft,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (app.lockMode == AppLockMode.PERMANENT) Color(0xFFD97706) else earth.camelOchre,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (app.lockMode == AppLockMode.PERMANENT) "🔒 24/7" else "☢️ Nuclear",
                                            color = if (app.lockMode == AppLockMode.PERMANENT) Color(0xFFFBBF24) else earth.forestDark,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = earth.textPrimary)
                    ) {
                        Text("Cancel", fontSize = 13.5.sp)
                    }

                    Button(
                        onClick = {
                            val selectedAppsList = installedApps
                                .filter { selectedPackages.contains(it.packageName) }
                                .map { it.packageName to it.appName }
                            onAppsSelected(selectedAppsList, selectedMode)
                        },
                        enabled = selectedPackages.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = earth.forestGreen,
                            disabledContainerColor = earth.surfaceSoft
                        )
                    ) {
                        Text(
                            text = if (selectedPackages.isEmpty()) "Select Apps" else "Lock ${selectedPackages.size} Apps",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedPackages.isEmpty()) earth.textMuted else Color.White
                        )
                    }
                }
            }
        }
    }
}
