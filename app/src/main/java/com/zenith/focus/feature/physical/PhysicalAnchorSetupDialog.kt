package com.zenith.focus.feature.physical

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.EarthTheme
import com.zenith.focus.domain.model.ProtectionConfig

@Composable
fun PhysicalAnchorSetupDialog(
    config: ProtectionConfig,
    scannedNfcTagHash: String? = null,
    onClearScannedNfcTag: () -> Unit = {},
    onSaveNfcHash: (String) -> Unit,
    onSaveQrHash: (String) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val earth = EarthTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isNfcSupported = remember { PhysicalAnchorManager.isNfcSupported(context) }

    LaunchedEffect(scannedNfcTagHash) {
        if (!scannedNfcTagHash.isNullOrBlank()) {
            onSaveNfcHash(scannedNfcTagHash)
            Toast.makeText(context, "✓ NFC Card registered successfully as focus key!", Toast.LENGTH_SHORT).show()
            onClearScannedNfcTag()
        }
    }

    var selectedTab by remember { mutableIntStateOf(if (isNfcSupported) 0 else 1) } // 0 = NFC, 1 = QR

    // QR state
    var generatedPayload by remember { mutableStateOf(PhysicalAnchorManager.generateZenithAnchorPayload()) }
    var qrBitmap by remember(generatedPayload) {
        mutableStateOf(QrCodeGenerator.generateQrBitmap(generatedPayload, 320))
    }
    var manualQrInput by remember { mutableStateOf("") }

    val hasRegisteredNfc = config.nfcAnchorHash.isNotBlank()
    val hasRegisteredQr = config.qrAnchorHash.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = earth.surfaceSoft,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, earth.border, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "🧱 PHYSICAL ANCHOR LOCK",
                    color = earth.forestGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DIY Hardware Key",
                    color = earth.forestDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Turn any physical object into a focus lock key. Requires tapping your card or scanning your QR key to unlock early.",
                    color = earth.textMuted,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Mode Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(earth.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (isNfcSupported) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) Color.White else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Nfc,
                                    contentDescription = "NFC",
                                    tint = if (selectedTab == 0) earth.forestDark else earth.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "NFC Card",
                                    color = if (selectedTab == 0) earth.forestDark else earth.textMuted,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) Color.White else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.QrCode,
                                contentDescription = "QR",
                                tint = if (selectedTab == 1) earth.forestDark else earth.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "QR Anchor",
                                color = if (selectedTab == 1) earth.forestDark else earth.textMuted,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (selectedTab == 0 && isNfcSupported) {
                    // NFC SECTION
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(if (hasRegisteredNfc) Color(0xFFE8F5E9) else earth.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasRegisteredNfc) Icons.Outlined.CheckCircle else Icons.Outlined.Nfc,
                            contentDescription = "NFC",
                            tint = if (hasRegisteredNfc) earth.forestGreen else earth.camelOchre,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (hasRegisteredNfc) "✓ NFC Card Linked" else "Ready to Link Card",
                        color = earth.forestDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Hold any transit card, college badge, or bank card against the back of this phone while on this screen.",
                        color = earth.textMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    if (hasRegisteredNfc) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "SHA-256: ${config.nfcAnchorHash.take(12)}... (Raw ID never stored)",
                            color = earth.textMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onSaveNfcHash("") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "Unlink NFC Card", fontSize = 11.sp, color = earth.camelOchre)
                        }
                    }
                } else {
                    // QR SECTION
                    if (qrBitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, earth.border, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Zenith Anchor QR Code",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                val hash = PhysicalAnchorManager.hashString(generatedPayload)
                                onSaveQrHash(hash)
                                clipboardManager.setText(AnnotatedString(generatedPayload))
                                Toast.makeText(context, "QR Anchor registered & payload copied!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = earth.forestGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(text = "Register This QR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                generatedPayload = PhysicalAnchorManager.generateZenithAnchorPayload()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Or enter existing QR text / code payload:",
                        color = earth.textMuted,
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = manualQrInput,
                        onValueChange = { manualQrInput = it },
                        placeholder = { Text("e.g. tape a QR on your fridge", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (manualQrInput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val hash = PhysicalAnchorManager.hashString(manualQrInput)
                                onSaveQrHash(hash)
                                Toast.makeText(context, "Custom QR payload registered!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = earth.camelOchre),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text(text = "Link Custom QR Payload", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (hasRegisteredQr) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ QR Anchor Key is active",
                            color = earth.forestGreen,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Done button
                Button(
                    onClick = {
                        if (hasRegisteredNfc || hasRegisteredQr) {
                            onToggleEnabled(true)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = earth.forestDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text(text = "SAVE & CLOSE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
