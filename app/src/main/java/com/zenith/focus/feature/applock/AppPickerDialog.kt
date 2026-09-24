package com.zenith.focus.feature.applock

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith.focus.core.designsystem.*
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(canvasBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                // Header with squircle close button and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.90f))
                                .border(1.dp, earth.border, RoundedCornerShape(14.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = earth.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "APP LOCK SHIELD",
                                color = OrbitalMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Select Apps to Lock",
                                color = earth.textPrimary,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (selectedPackages.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(OrbitalCoral)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${selectedPackages.size} Selected",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Selector Segmented Cards
                Text(
                    text = "TARGET LOCK MODE FOR SELECTED APPS:",
                    color = earth.textMuted,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Nuclear Mode Chip
                    val isNuclearSelected = selectedMode == AppLockMode.NUCLEAR_ONLY
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isNuclearSelected) Color.White else Color.White.copy(alpha = 0.60f))
                            .border(
                                width = if (isNuclearSelected) 1.5.dp else 1.dp,
                                color = if (isNuclearSelected) OrbitalViolet else earth.border,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedMode = AppLockMode.NUCLEAR_ONLY }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "☢️ Nuclear Mode",
                                color = if (isNuclearSelected) OrbitalViolet else earth.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isNuclearSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "During Nuclear sessions",
                                color = earth.textMuted,
                                fontSize = 10.5.sp,
                                maxLines = 1
                            )
                        }
                    }

                    // Permanent Mode Chip
                    val isPermanentSelected = selectedMode == AppLockMode.PERMANENT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isPermanentSelected) Color.White else Color.White.copy(alpha = 0.60f))
                            .border(
                                width = if (isPermanentSelected) 1.5.dp else 1.dp,
                                color = if (isPermanentSelected) OrbitalAmber else earth.border,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedMode = AppLockMode.PERMANENT }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🔒 Permanent 24/7",
                                color = if (isPermanentSelected) OrbitalAmber else earth.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isPermanentSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Continuous around-the-clock",
                                color = earth.textMuted,
                                fontSize = 10.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps...", fontSize = 12.5.sp, color = earth.textMuted) },
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
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) earth.textPrimary else Color.White.copy(alpha = 0.60f))
                                    .border(1.dp, if (isSelected) earth.textPrimary else earth.border, RoundedCornerShape(10.dp))
                                    .clickable { filterTab = index }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
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
                        color = OrbitalCoral,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                selectedPackages = if (selectedPackages.size == filteredApps.size) {
                                    emptySet()
                                } else {
                                    filteredApps.map { it.packageName }.toSet()
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
                            CircularProgressIndicator(color = OrbitalCoral, modifier = Modifier.size(32.dp))
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isChecked = selectedPackages.contains(app.packageName)
                            val isAlreadyLocked = app.isLocked

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isChecked) Color.White else Color.White.copy(alpha = 0.85f))
                                    .border(
                                        width = if (isChecked) 1.5.dp else 1.dp,
                                        color = if (isChecked) OrbitalCoral else earth.border,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        selectedPackages = if (isChecked) {
                                            selectedPackages - app.packageName
                                        } else {
                                            selectedPackages + app.packageName
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                        checkedColor = OrbitalCoral,
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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (app.lockMode == AppLockMode.PERMANENT) OrbitalAmber.copy(alpha = 0.15f)
                                                else OrbitalViolet.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (app.lockMode == AppLockMode.PERMANENT) "🔒 Perm" else "☢️ Nuclear",
                                            color = if (app.lockMode == AppLockMode.PERMANENT) OrbitalAmber else OrbitalViolet,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Confirm Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = earth.textPrimary, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val selectedAppList = installedApps
                                .filter { selectedPackages.contains(it.packageName) }
                                .map { it.packageName to it.appName }
                            onAppsSelected(selectedAppList, selectedMode)
                        },
                        enabled = selectedPackages.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = OrbitalCoral),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.6f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedPackages.isEmpty()) "Select Apps" else "Lock (${selectedPackages.size})",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
