package com.zenith.focus.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zenith.focus.R
import com.zenith.focus.core.designsystem.EarthBorderLinen
import com.zenith.focus.core.designsystem.EarthCamelOchre
import com.zenith.focus.core.designsystem.EarthCanvasCream
import com.zenith.focus.core.designsystem.EarthForestDark
import com.zenith.focus.core.designsystem.EarthForestGreen
import com.zenith.focus.core.designsystem.EarthSurfaceLinen
import com.zenith.focus.core.designsystem.EarthSurfaceLinenSoft
import com.zenith.focus.core.designsystem.EarthTextDark
import com.zenith.focus.core.designsystem.EarthTextMuted
import com.zenith.focus.core.ui.RestrictedSettingsBanner
import com.zenith.focus.core.ui.RestrictedSettingsGuideDialog
import com.zenith.focus.domain.model.FrictionType
import com.zenith.focus.domain.model.ProtectionConfig
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver

@Composable
fun SettingsScreen(
    config: ProtectionConfig,
    isServiceConnected: Boolean,
    isNuclearActive: Boolean = false,
    currentTheme: String = "",
    onSelectFrictionType: (FrictionType) -> Unit = {},
    onSetPin: suspend (String) -> Unit = {},
    onClearPin: suspend () -> Unit = {},
    onSetTheme: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    var showRestrictedDialog by remember { mutableStateOf(false) }
    var isDeviceAdminActive by remember {
        mutableStateOf(ZenithDeviceAdminReceiver.isAdminActive(context))
    }

    if (showRestrictedDialog) {
        RestrictedSettingsGuideDialog(onDismiss = { showRestrictedDialog = false })
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isDeviceAdminActive = ZenithDeviceAdminReceiver.isAdminActive(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EarthCanvasCream)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "SECURITY & CONTROLS",
            color = EarthForestGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Settings",
            color = EarthForestDark,
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "Manage system permissions and anti-impulse unlock barriers.",
            color = EarthTextMuted,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // NUCLEAR MODE ACTIVE BANNER (IF ACTIVE)
        if (isNuclearActive) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, EarthCamelOchre, RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "☢️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NUCLEAR MODE ACTIVE",
                            color = EarthCamelOchre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Security and unlock friction settings are locked until the session expires.",
                            color = EarthTextDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // SYSTEM STATUS
        Text(
            text = "SYSTEM STATUS",
            color = EarthForestGreen,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Accessibility Service Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accessibility Shield Service", color = EarthTextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isServiceConnected) "Online: Instant zero-tolerance ejection active" else "Inactive: Required for short-form video blocking",
                            color = if (isServiceConnected) EarthForestGreen else Color(0xFFB91C1C),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isServiceConnected) EarthForestGreen else Color(0xFFB91C1C))
                    )
                }

                if (!isServiceConnected) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EarthForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ACTIVATE ACCESSIBILITY PERMISSION", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    RestrictedSettingsBanner(
                        onOpenDialog = { showRestrictedDialog = true }
                    )
                }
            }
        }

        // Uninstall Protection (Device Administrator) Card
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Uninstall Protection (Device Admin)", color = EarthTextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isDeviceAdminActive) "Active: OS-level deletion block armed" else "Inactive: Grant Device Admin to prevent uninstallation",
                            color = if (isDeviceAdminActive) EarthForestGreen else EarthCamelOchre,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isDeviceAdminActive) EarthForestGreen else EarthCamelOchre)
                    )
                }

                if (!isDeviceAdminActive) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            ZenithDeviceAdminReceiver.openDeviceAdminActivation(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EarthCamelOchre),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ACTIVATE UNINSTALL PROTECTION", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    RestrictedSettingsBanner(
                        onOpenDialog = { showRestrictedDialog = true }
                    )
                } else if (isNuclearActive) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🔒 Locked by Nuclear Mode. Deactivation is forbidden until session expires.",
                        color = EarthCamelOchre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DEVELOPER & SUPPORT
        Text(
            text = "DEVELOPER & SUPPORT",
            color = EarthForestGreen,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EarthSurfaceLinen)
                            .border(1.5.dp, EarthCamelOchre, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👨‍💻", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Sachindra Shekhar Pandey",
                            color = EarthForestDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Creator & Lead Android Engineer",
                            color = EarthCamelOchre,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email Support
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EarthSurfaceLinen)
                        .border(1.dp, EarthBorderLinen, RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:Sachindrapandey328@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Reel Narcotics Support & Feedback")
                            }
                            context.startActivity(intent)
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✉️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Support Email", color = EarthTextMuted, fontSize = 11.sp)
                        Text(text = "Sachindrapandey328@gmail.com", color = EarthTextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "➔", color = EarthCamelOchre, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GitHub
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EarthSurfaceLinen)
                        .border(1.dp, EarthBorderLinen, RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sachindrapandeyyy"))
                            context.startActivity(intent)
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🐙", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "GitHub Repository & Profile", color = EarthTextMuted, fontSize = 11.sp)
                        Text(text = "github.com/Sachindrapandeyyy", color = EarthTextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "➔", color = EarthCamelOchre, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LinkedIn
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EarthSurfaceLinen)
                        .border(1.dp, EarthBorderLinen, RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/sachindra-shekhar-pandey-73b45427b/"))
                            context.startActivity(intent)
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💼", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "LinkedIn Profile", color = EarthTextMuted, fontSize = 11.sp)
                        Text(text = "Sachindra Shekhar Pandey", color = EarthTextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "➔", color = EarthCamelOchre, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ABOUT & PRIVACY GUARANTEE
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinenSoft),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EarthBorderLinen, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_reel_narcotics_logo),
                    contentDescription = "Reel Narcotics Logo",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, EarthBorderLinen, RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Reel Narcotics v2.2.0", color = EarthForestDark, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    Text("Break the scroll. Take back your attention.", color = EarthForestGreen, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "100% Offline | Zero Telemetry | Zero Accounts\nNo internet permission requested or needed.",
                        color = EarthTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}
