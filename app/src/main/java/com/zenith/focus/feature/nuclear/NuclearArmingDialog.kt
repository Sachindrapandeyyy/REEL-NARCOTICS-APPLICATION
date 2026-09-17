package com.zenith.focus.feature.nuclear

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith.focus.core.designsystem.*
import com.zenith.focus.core.time.DateTimeUtils
import com.zenith.focus.domain.model.ContentCategory
import com.zenith.focus.receiver.ZenithDeviceAdminReceiver
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NuclearArmingDialog(
    onDismiss: () -> Unit,
    onArmSession: (durationMillis: Long, enabledCategories: Set<ContentCategory>) -> Unit,
    onConfirmActivation: () -> Unit
) {
    val earth = EarthTheme.colors
    val EarthSurfaceLinenSoft = earth.surfaceSoft
    val EarthSurfaceLinen = earth.surface
    val EarthBorderLinen = earth.border
    val EarthTextDark = earth.textPrimary
    val EarthTextMuted = earth.textMuted
    val EarthForestDark = earth.forestDark
    val EarthForestGreen = earth.forestGreen
    val EarthCamelOchre = earth.camelOchre

    var step by remember { mutableIntStateOf(0) } // 0: Config & Shields, 1: Ready & Hold to Activate
    var selectedDurationMillis by remember { mutableLongStateOf(2 * 60 * 60 * 1000L) } // default 2 hours

    // Selective Nuclear Lock Shields: User decides what to lock
    var selectedCategories by remember {
        mutableStateOf(
            setOf(
                ContentCategory.YOUTUBE_SHORTS,
                ContentCategory.INSTAGRAM_REELS,
                ContentCategory.FACEBOOK_REELS,
                ContentCategory.SNAPCHAT_SPOTLIGHT,
                ContentCategory.ADULT_WEBSITE,
                ContentCategory.ADULT_KEYWORD
            )
        )
    }

    // Extended Custom Timer: Days (0-90), Hours (0-23), Minutes (0-55)
    var customDays by remember { mutableIntStateOf(0) }
    var customHours by remember { mutableIntStateOf(2) }
    var customMinutes by remember { mutableIntStateOf(0) }
    var isCustomPickerOpen by remember { mutableStateOf(false) }

    val view = LocalView.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()

    var isAdminActive by remember { mutableStateOf(ZenithDeviceAdminReceiver.isAdminActive(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAdminActive = ZenithDeviceAdminReceiver.isAdminActive(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val durations = remember {
        listOf(
            "5 MIN" to (5 * 60 * 1000L),
            "15 MIN" to (15 * 60 * 1000L),
            "30 MIN" to (30 * 60 * 1000L),
            "1 HOUR" to (60 * 60 * 1000L),
            "2 HOURS" to (2 * 60 * 60 * 1000L),
            "6 HOURS" to (6 * 60 * 60 * 1000L),
            "24 HOURS" to (24 * 60 * 60 * 1000L),
            "7 DAYS" to (7 * 24 * 60 * 60 * 1000L),
            "30 DAYS (1 MO)" to (30 * 24 * 60 * 60 * 1000L),
            "90 DAYS (3 MO)" to (90 * 24 * 60 * 60 * 1000L)
        )
    }

    val targetTimeFormatted = remember(selectedDurationMillis) {
        val target = System.currentTimeMillis() + selectedDurationMillis
        SimpleDateFormat("h:mm a (MMM d, yyyy)", Locale.getDefault()).format(Date(target))
    }

    Dialog(onDismissRequest = {
        if (step == 0) onDismiss() else step = 0
    }) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = EarthSurfaceLinenSoft,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(22.dp)
            ) {
                if (step == 0) {
                    // STEP 1: WARNING, SELECTIVE SHIELDS & DURATION SELECTION
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EarthCamelOchre.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "☢️", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NUCLEAR MODE",
                            color = EarthCamelOchre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Custom Nuclear Lock",
                        color = EarthForestDark,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    )

                    // EXPLANATION CARD
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .border(1.dp, EarthCamelOchre.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "STRICT IRREVERSIBLE COMMITMENT:",
                                color = EarthCamelOchre,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• You choose what to lock below. Selected feeds are 100% blocked.\n" +
                                       "• Unselected apps remain accessible for your workflow.\n" +
                                       "• Once armed, the lock CANNOT be stopped early.\n" +
                                       "• Anti-tamper & reboot persistence active for locked items.",
                                color = EarthTextDark,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    // =========================================================
                    // FEATURE 1: SELECTIVE NUCLEAR SHIELDS
                    // =========================================================
                    Text(
                        text = "CHOOSE WHAT TO LOCK 🛡️",
                        color = EarthForestGreen,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tap to enable/disable shields for this Nuclear Lock session:",
                        color = EarthTextMuted,
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    val shieldOptions = listOf(
                        Triple(ContentCategory.YOUTUBE_SHORTS, "YouTube Shorts", "🎬"),
                        Triple(ContentCategory.INSTAGRAM_REELS, "Instagram Reels", "📸"),
                        Triple(ContentCategory.FACEBOOK_REELS, "Facebook Reels", "📘"),
                        Triple(ContentCategory.SNAPCHAT_SPOTLIGHT, "Snapchat Spotlight", "👻"),
                        Triple(ContentCategory.ADULT_WEBSITE, "Adult & Explicit Sites", "🔞")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        shieldOptions.forEach { (category, title, icon) ->
                            val isSelected = selectedCategories.contains(category) ||
                                (category == ContentCategory.ADULT_WEBSITE && selectedCategories.contains(ContentCategory.ADULT_KEYWORD))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) EarthCamelOchre.copy(alpha = 0.15f) else EarthSurfaceLinen)
                                    .border(
                                        1.dp,
                                        if (isSelected) EarthCamelOchre else EarthBorderLinen,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        val newSet = selectedCategories.toMutableSet()
                                        if (isSelected) {
                                            newSet.remove(category)
                                            if (category == ContentCategory.ADULT_WEBSITE) {
                                                newSet.remove(ContentCategory.ADULT_KEYWORD)
                                            }
                                        } else {
                                            newSet.add(category)
                                            if (category == ContentCategory.ADULT_WEBSITE) {
                                                newSet.add(ContentCategory.ADULT_KEYWORD)
                                            }
                                        }
                                        selectedCategories = newSet
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = icon, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = title,
                                            color = if (isSelected) EarthForestDark else EarthTextMuted,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }

                                    Text(
                                        text = if (isSelected) "LOCK 🔒" else "OPEN 🔓",
                                        color = if (isSelected) EarthCamelOchre else EarthTextMuted,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // =========================================================
                    // FEATURE 2: DURATION SELECTION (UP TO 3 MONTHS / 90 DAYS)
                    // =========================================================
                    Text(
                        text = "SELECT COMMITMENT DURATION",
                        color = EarthForestGreen,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // DURATION CHIPS (Rows of 2)
                    for (i in durations.indices step 2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val item1 = durations[i]
                            val isSel1 = !isCustomPickerOpen && selectedDurationMillis == item1.second
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel1) EarthCamelOchre else EarthSurfaceLinen)
                                    .border(
                                        1.dp,
                                        if (isSel1) EarthCamelOchre else EarthBorderLinen,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        isCustomPickerOpen = false
                                        selectedDurationMillis = item1.second
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item1.first,
                                    color = if (isSel1) Color.White else EarthTextDark,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (i + 1 < durations.size) {
                                val item2 = durations[i + 1]
                                val isSel2 = !isCustomPickerOpen && selectedDurationMillis == item2.second
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel2) EarthCamelOchre else EarthSurfaceLinen)
                                        .border(
                                            1.dp,
                                            if (isSel2) EarthCamelOchre else EarthBorderLinen,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            isCustomPickerOpen = false
                                            selectedDurationMillis = item2.second
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item2.first,
                                        color = if (isSel2) Color.White else EarthTextDark,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // CUSTOM DURATION BUTTON
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCustomPickerOpen) EarthForestGreen else EarthSurfaceLinen)
                            .border(1.dp, if (isCustomPickerOpen) EarthForestGreen else EarthBorderLinen, RoundedCornerShape(10.dp))
                            .clickable {
                                isCustomPickerOpen = !isCustomPickerOpen
                                if (isCustomPickerOpen) {
                                    val total = (customDays * 86400000L) + (customHours * 3600000L) + (customMinutes * 60000L)
                                    selectedDurationMillis = total.coerceAtLeast(1 * 60000L)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCustomPickerOpen) "CUSTOM DURATION ACTIVE ⏱️" else "SET CUSTOM DURATION (UP TO 3 MONTHS) ⏱️",
                            color = if (isCustomPickerOpen) Color.White else EarthForestDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isCustomPickerOpen) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, EarthBorderLinen, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // DAYS (0-90)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("DAYS", color = EarthTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(earth.canvasElevated)
                                                    .border(1.dp, earth.border, CircleShape)
                                                    .clickable {
                                                        if (customDays > 0) {
                                                            customDays--
                                                            val total = (customDays * 86400000L) + (customHours * 3600000L) + (customMinutes * 60000L)
                                                            selectedDurationMillis = total.coerceAtLeast(1 * 60000L)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("-", color = EarthTextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            Text(
                                                text = "${customDays}d",
                                                color = EarthTextDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(earth.canvasElevated)
                                                    .border(1.dp, earth.border, CircleShape)
                                                    .clickable {
                                                        if (customDays < 90) {
                                                            customDays++
                                                            val total = (customDays * 86400000L) + (customHours * 3600000L) + (customMinutes * 60000L)
                                                            selectedDurationMillis = total.coerceAtLeast(1 * 60000L)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("+", color = EarthTextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                        }
                                    }

                                    // HOURS (0-23)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("HOURS", color = EarthTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(earth.canvasElevated)
                                                    .border(1.dp, earth.border, CircleShape)
                                                    .clickable {
                                                        if (customHours > 0) {
                                                            customHours--
                                                            val total = (customDays * 86400000L) + (customHours * 3600000L) + (customMinutes * 60000L)
                                                            selectedDurationMillis = total.coerceAtLeast(1 * 60000L)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("-", color = EarthTextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            Text(
                                                text = "${customHours}h",
                                                color = EarthTextDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(earth.canvasElevated)
                                                    .border(1.dp, earth.border, CircleShape)
                                                    .clickable {
                                                        if (customHours < 23) {
                                                            customHours++
                                                            val total = (customDays * 86400000L) + (customHours * 3600000L) + (customMinutes * 60000L)
                                                            selectedDurationMillis = total.coerceAtLeast(1 * 60000L)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("+", color = EarthTextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                        }
                                    }

                                    // MINUTES (0-55)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("MINUTES", color = EarthTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(earth.canvasElevated)
                                                    .border(1.dp, earth.border, CircleShape)
                                                    .clickable {
                                                        if (customMinutes >= 5) {
                                                            customMinutes -= 5
                                                            val total = (customDays * 86400000L) + (customHours * 3600000L) + (customMinutes * 60000L)
                                                            selectedDurationMillis = total.coerceAtLeast(1 * 60000L)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("-", color = EarthTextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                            Text(
                                                text = "${customMinutes}m",
                                                color = EarthTextDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(earth.canvasElevated)
                                                    .border(1.dp, earth.border, CircleShape)
                                                    .clickable {
                                                        if (customMinutes < 55) {
                                                            customMinutes += 5
                                                            val total = (customDays * 86400000L) + (customHours * 3600000L) + (customMinutes * 60000L)
                                                            selectedDurationMillis = total.coerceAtLeast(1 * 60000L)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("+", color = EarthTextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TARGET EXPIRY PREVIEW
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EarthSurfaceLinen)
                            .border(1.dp, EarthBorderLinen, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏳ Target End: $targetTimeFormatted",
                            color = EarthCamelOchre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isAdminActive) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, EarthCamelOchre.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🔒 Uninstall Protection Required",
                                    color = EarthForestDark,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Nuclear Mode cannot be uninstalled or modified while your timer runs. Please activate Device Administrator to seal the lock.",
                                    color = EarthTextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = { ZenithDeviceAdminReceiver.openDeviceAdminActivation(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EarthCamelOchre),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text("ACTIVATE UNINSTALL PROTECTION ➔", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(text = "CANCEL", color = EarthTextMuted, fontWeight = FontWeight.Bold)
                        }

                        val canProceed = selectedCategories.isNotEmpty()
                        Button(
                            onClick = {
                                if (canProceed) {
                                    if (!isAdminActive) {
                                        ZenithDeviceAdminReceiver.openDeviceAdminActivation(context)
                                    } else {
                                        onArmSession(selectedDurationMillis, selectedCategories)
                                        step = 1
                                    }
                                }
                            },
                            enabled = canProceed,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EarthCamelOchre),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (!canProceed) "SELECT A SHIELD" else if (!isAdminActive) "1. ACTIVATE PROTECTION" else "PROCEED ➔",
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                } else {
                    // STEP 2: FINAL CONFIRMATION + HOLD TO ACTIVATE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EarthCamelOchre.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "☢️", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FINAL CONFIRMATION",
                            color = EarthCamelOchre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EarthSurfaceLinen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EarthBorderLinen, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Commitment Duration:", color = EarthTextMuted, fontSize = 12.sp)
                                Text(
                                    text = DateTimeUtils.formatRemaining(selectedDurationMillis),
                                    color = EarthForestDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Ends at:", color = EarthTextMuted, fontSize = 12.sp)
                                Text(
                                    text = targetTimeFormatted,
                                    color = EarthCamelOchre,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "LOCKED SHIELDS (${selectedCategories.size}):",
                                color = EarthForestGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedCategories.joinToString(", ") { it.displayName },
                                color = EarthTextDark,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "⚠️ Once activated, you CANNOT cancel or unlock until the timer reaches zero. Hold below to seal your focus.",
                        color = EarthCamelOchre,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // HOLD TO ACTIVATE BUTTON (3 SECONDS)
                    HoldToActivateButton(
                        onConfirmed = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onConfirmActivation()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { step = 0 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "BACK TO DURATION & SHIELDS", color = EarthTextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HoldToActivateButton(
    onConfirmed: () -> Unit
) {
    val earth = EarthTheme.colors
    val EarthCamelOchre = earth.camelOchre
    val EarthBorderLinen = earth.border
    val EarthForestDark = earth.forestDark

    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }
    val view = LocalView.current

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val totalSteps = 30
            val stepDelay = 100L // 3000ms total
            for (i in 1..totalSteps) {
                if (!isHolding) break
                delay(stepDelay)
                holdProgress = i.toFloat() / totalSteps.toFloat()
                if (i % 10 == 0) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            if (isHolding && holdProgress >= 1f) {
                onConfirmed()
            }
        } else {
            holdProgress = 0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(100),
        label = "HoldProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(EarthCamelOchre.copy(alpha = 0.2f))
            .border(2.dp, if (isHolding) EarthCamelOchre else EarthBorderLinen, RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isHolding = true
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    isHolding = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Fill Progress Background
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(60.dp)
                .background(EarthCamelOchre)
                .align(Alignment.CenterStart)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isHolding) "KEEP HOLDING... (${(animatedProgress * 100).toInt()}%)" else "HOLD TO ACTIVATE ☢️",
                color = if (animatedProgress > 0.4f) Color.White else EarthForestDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}
