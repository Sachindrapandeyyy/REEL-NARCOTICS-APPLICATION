# ProGuard / R8 rules for Reel Narcotics (Zenith Focus)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

# --- In-App Self-Updater & JSON Models ---
-keep class com.zenith.focus.core.update.UpdateManifest { *; }
-keep class com.zenith.focus.core.update.ApkMetadata { *; }
-keep class com.zenith.focus.core.update.UpdateException** { *; }
-keep class com.zenith.focus.core.update.UpdateState** { *; }
-keep class com.zenith.focus.core.update.UpdateManager** { *; }

# --- Security, Receivers, Services & Device Admin ---
-keep class com.zenith.focus.receiver.** { *; }
-keep class com.zenith.focus.feature.nuclear.NuclearForegroundService { *; }
-keep class com.zenith.focus.core.security.DeviceOwnerManager { *; }

# --- Accessibility Service & Overlays ---
-keep class com.zenith.focus.accessibility.service.ZenithAccessibilityService { *; }
-keep class com.zenith.focus.accessibility.overlay.** { *; }

# --- Psychology Engine & Activities ---
-keep class com.zenith.focus.feature.psychology.** { *; }

# --- Core App & Dependency Injection Container ---
-keep class com.zenith.focus.ZenithApplication { *; }
-keep class com.zenith.focus.core.di.ZenithAppContainer { *; }

# --- DataStore, Database & Domain Models ---
-keep class com.zenith.focus.domain.model.** { *; }
-keep class com.zenith.focus.domain.nuclear.** { *; }
-keep class com.zenith.focus.data.local.** { *; }

# --- Physical & Sensory Armor (v3.0.0) ---
-keep class com.zenith.focus.feature.physical.** { *; }
-keep class com.zenith.focus.feature.sensory.** { *; }
-keep class com.zenith.focus.feature.stats.FocusScoreCalculator** { *; }

# --- ZXing Core QR Code Generation ---
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

