package com.zenith.focus.core.update.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.EarthTheme
import com.zenith.focus.core.update.UpdateException
import com.zenith.focus.core.update.UpdateManifest
import com.zenith.focus.core.update.UpdateState
import java.io.File

@Composable
fun UpdateDialog(
    state: UpdateState,
    currentVersionName: String,
    onDismiss: () -> Unit,
    onStartDownload: (UpdateManifest) -> Unit,
    onInstall: (File) -> Unit,
    onOpenSettings: () -> Unit,
    canInstallPackages: Boolean,
    onRetry: () -> Unit
) {
    val earth = EarthTheme.colors

    Dialog(onDismissRequest = {
        if (state !is UpdateState.Downloading && state !is UpdateState.Verifying) {
            onDismiss()
        }
    }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = earth.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, earth.border, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (state) {
                    is UpdateState.Available -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📦", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.isMandatory) "Mandatory Update" else "Update Available",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = if (state.isMandatory) earth.error else earth.forestDark
                                )
                                Text(
                                    text = "Version ${state.manifest.versionName} is ready to download",
                                    fontSize = 12.sp,
                                    color = earth.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Version badge row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(earth.surfaceSoft)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CURRENT", fontSize = 10.sp, color = earth.textMuted, fontWeight = FontWeight.Bold)
                                Text("v$currentVersionName", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = earth.textPrimary)
                            }
                            Text("➔", fontSize = 16.sp, color = earth.camelOchre)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("NEW VERSION", fontSize = 10.sp, color = earth.forestGreen, fontWeight = FontWeight.Bold)
                                Text("v${state.manifest.versionName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = earth.forestDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val sizeMb = String.format("%.1f", state.manifest.apk.sizeBytes / (1024.0 * 1024.0))
                        Text(
                            text = "Download size: $sizeMb MB",
                            fontSize = 12.sp,
                            color = earth.textMuted
                        )

                        if (state.manifest.releaseNotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Release Notes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = earth.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(earth.surfaceSoft)
                                    .padding(12.dp)
                            ) {
                                state.manifest.releaseNotes.forEach { note ->
                                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                        Text("• ", color = earth.forestGreen, fontWeight = FontWeight.Bold)
                                        Text(note, fontSize = 12.sp, color = earth.textPrimary, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            if (!state.isMandatory) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = earth.textMuted)
                                ) {
                                    Text("Later", fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Button(
                                onClick = { onStartDownload(state.manifest) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = earth.forestDark,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Download", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is UpdateState.Downloading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏳", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Downloading Update",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = earth.forestDark
                                )
                                Text(
                                    text = "${state.progressPercent}% completed",
                                    fontSize = 12.sp,
                                    color = earth.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        LinearProgressIndicator(
                            progress = state.progressPercent / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = earth.forestGreen,
                            trackColor = earth.surfaceSoft
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val downloadedMb = String.format("%.1f", state.bytesDownloaded / (1024.0 * 1024.0))
                        val totalMb = String.format("%.1f", state.totalBytes / (1024.0 * 1024.0))
                        Text(
                            text = "$downloadedMb MB / $totalMb MB",
                            fontSize = 12.sp,
                            color = earth.textMuted,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    is UpdateState.Verifying -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = earth.forestGreen
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Verifying Integrity",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = earth.forestDark
                                )
                                Text(
                                    text = "Checking SHA-256 and cryptographic signing...",
                                    fontSize = 12.sp,
                                    color = earth.textMuted
                                )
                            }
                        }
                    }

                    is UpdateState.ReadyToInstall -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ready to Install",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = earth.forestDark
                                )
                                Text(
                                    text = "Verified successfully. Tap below to confirm update.",
                                    fontSize = 12.sp,
                                    color = earth.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!canInstallPackages) {
                            Text(
                                text = "Notice: Android requires permission to update from outside app stores.",
                                fontSize = 12.sp,
                                color = earth.error,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onOpenSettings,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = earth.camelOchre, contentColor = Color.White)
                            ) {
                                Text("Grant Update Permission ➔", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = { onInstall(state.apkFile) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = earth.forestDark,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Install Update Now ➔", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    is UpdateState.Failed -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Update Failed",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = earth.error
                                )
                                Text(
                                    text = getErrorMessage(state.error),
                                    fontSize = 12.sp,
                                    color = earth.textMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = earth.textMuted)
                            ) {
                                Text("Dismiss", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = earth.forestDark,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Retry", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    is UpdateState.Installing -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = earth.forestGreen
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Launching Installer...",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = earth.forestDark
                                )
                                Text(
                                    text = "Confirm the system prompt to finish updating.",
                                    fontSize = 12.sp,
                                    color = earth.textMuted
                                )
                            }
                        }
                    }

                    is UpdateState.UpToDate -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Up to Date",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = earth.forestDark
                                )
                                Text(
                                    text = "Reel Narcotics is on the latest version (v${state.currentVersionName}).",
                                    fontSize = 12.sp,
                                    color = earth.textMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = earth.forestDark,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Got it", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    is UpdateState.Checking -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = earth.forestGreen
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Checking for Updates...",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = earth.forestDark
                                )
                                Text(
                                    text = "Connecting to release server...",
                                    fontSize = 12.sp,
                                    color = earth.textMuted
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

private fun getErrorMessage(error: UpdateException): String {
    return when (error) {
        is UpdateException.NetworkException -> "Network error: unable to connect to update server. Please check your internet connection."
        is UpdateException.ChecksumMismatchException -> "Security verification failed: checksum mismatch. The download may be corrupted."
        is UpdateException.PackageMismatchException -> "Security verification failed: package mismatch."
        is UpdateException.SignatureMismatchException -> "Security verification failed: cryptographic signing key does not match."
        is UpdateException.StorageException -> "Insufficient storage space on device."
        is UpdateException.ManifestException -> "Invalid update metadata received from server."
        is UpdateException.InstallationException -> "Unable to launch system package installer: ${error.localizedMessage}"
        is UpdateException.SecurityVerificationException -> "Security check failed: ${error.localizedMessage}"
    }
}
