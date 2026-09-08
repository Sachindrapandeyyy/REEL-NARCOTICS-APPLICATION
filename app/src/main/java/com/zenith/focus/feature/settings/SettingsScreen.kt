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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zenith.focus.R
import com.zenith.focus.core.designsystem.ZenithBurgundy
import com.zenith.focus.core.designsystem.ZenithBurgundyDeep
import com.zenith.focus.core.designsystem.ZenithEmerald
import com.zenith.focus.core.designsystem.ZenithEmeraldAccent
import com.zenith.focus.core.designsystem.ZenithNavy
import com.zenith.focus.core.designsystem.ZenithNavyDark
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
            .background(ZenithNavyDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "SECURITY & CONTROLS",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Manage system permissions and anti-impulse unlock barriers.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // NUCLEAR MODE ACTIVE BANNER (IF ACTIVE)
        if (isNuclearActive) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ZenithBurgundyDeep),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ZenithBurgundy, RoundedCornerShape(16.dp))
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
                            color = Color(0xFFFECACA),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Security and unlock friction settings are locked until the session expires.",
                            color = Color(0xFFE2E8F0),
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
            color = ZenithEmeraldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Accessibility Service Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ZenithNavy),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accessibility Shield Service", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isServiceConnected) "Online: Instant zero-tolerance ejection active" else "Inactive: Required for short-form video blocking",
                            color = if (isServiceConnected) ZenithEmeraldAccent else Color(0xFFF43F5E),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isServiceConnected) ZenithEmeraldAccent else Color(0xFFF43F5E))
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ACTIVATE ACCESSIBILITY PERMISSION", fontWeight = FontWeight.Bold)
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ZenithNavy),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Uninstall Protection (Device Admin)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isDeviceAdminActive) "Active: OS-level deletion block armed" else "Inactive: Grant Device Admin to prevent uninstallation",
                            color = if (isDeviceAdminActive) ZenithEmeraldAccent else Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isDeviceAdminActive) ZenithEmeraldAccent else Color(0xFFF59E0B))
                    )
                }

                if (!isDeviceAdminActive) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            ZenithDeviceAdminReceiver.openDeviceAdminActivation(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ACTIVATE UNINSTALL PROTECTION", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    RestrictedSettingsBanner(
                        onOpenDialog = { showRestrictedDialog = true }
                    )
                } else if (isNuclearActive) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🔒 Locked by Nuclear Mode. Deactivation is forbidden until session expires.",
                        color = Color(0xFFFECACA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // DEVELOPER & SUPPORT
        Text(
            text = "DEVELOPER & SUPPORT",
            color = Color(0xFF38BDF8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
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
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, ZenithEmeraldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👨‍💻", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sachindra Shekhar Pandey",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Creator & Lead Android Engineer",
                            color = ZenithEmeraldAccent,
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
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
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
                        Text(text = "Support Email", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "Sachindrapandey328@gmail.com", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "➔", color = ZenithEmeraldAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GitHub
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
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
                        Text(text = "GitHub Repository & Profile", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "github.com/Sachindrapandeyyy", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "➔", color = ZenithEmeraldAccent, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LinkedIn
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
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
                        Text(text = "LinkedIn Profile", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "Sachindra Shekhar Pandey", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "➔", color = ZenithEmeraldAccent, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ABOUT & PRIVACY GUARANTEE
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ZenithNavy),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
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
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Reel Narcotics v2.0.1", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    Text("Break the scroll. Take back your attention.", color = ZenithEmeraldAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "100% Offline | Zero Telemetry | Zero Accounts\nNo internet permission requested or needed.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
