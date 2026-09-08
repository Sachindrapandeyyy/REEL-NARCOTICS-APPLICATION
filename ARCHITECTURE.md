# Zenith Focus Architecture ???

Zenith Focus is built using **Modern Android Clean Architecture**, **MVVM**, and the **Repository Pattern** with Kotlin Coroutines and StateFlow.

---

## 1. High-Level Architectural Layers

`
+-------------------------------------------------------------+
¦                    UI & Presentation Layer                  ¦
¦  - Jetpack Compose Screens (Home, Shield, Stats, Settings)  ¦
¦  - ZenithMascot Vector Canvas Renderer (7 reactive states)  ¦
¦  - LockSetupDialog & UnlockFrictionDialog                   ¦
¦  - OnboardingFlow & Fullscreen Fallback Activity            ¦
+-------------------------------------------------------------+
                               ¦ Observes StateFlows
+------------------------------?------------------------------+
¦                      Domain Logic Layer                     ¦
¦  - Models (LockState, ProtectionConfig, BlockEvent, etc.)   ¦
¦  - Repository Interfaces (LockRepository, Settings, Stats)  ¦
¦  - ContentDetector Interfaces & Confidence Scoring Rules    ¦
¦  - Security Engine (PinHasher PBKDF2-HMAC-SHA256)           ¦
¦  - Time Calculations (DateTimeUtils, Absolute Timestamps)   ¦
+-------------------------------------------------------------+
                               ¦ Implements
+------------------------------?------------------------------+
¦                    Data & Persistence Layer                 ¦
¦  - Jetpack DataStore Preferences (LockState & Settings)     ¦
¦  - SQLite Database Helper (Local relational block events)   ¦
¦  - ZenithAppContainer (Lightweight Service Locator / DI)    ¦
+-------------------------------------------------------------+
                               ¦ Consumed by
+------------------------------?------------------------------+
¦                Accessibility & Enforcement Layer            ¦
¦  - ZenithAccessibilityService (Event queue, debouncing)     ¦
¦  - HierarchyTraverser (Bounded BFS, max depth 10, 150 nodes)¦
¦  - TextNormalizer (Unicode NFKD, leetspeak decoder)         ¦
¦  - DetectionEngine (Modular multi-signal detectors)         ¦
¦  - OverlayWindowManager (TYPE_APPLICATION_OVERLAY / fallback)¦
+-------------------------------------------------------------+
`

---

## 2. Absolute-Timestamp Lock Engine

To survive process death and phone reboots, Zenith Focus **never** decrements a volatile in-memory countdown counter (emainingSeconds--).

Instead, when a lock is initiated:
1. startTimeMillis = System.currentTimeMillis()
2. endTimeMillis = startTimeMillis + durationMillis
3. 	imeZoneId = TimeZone.getDefault().id
4. State is atomically persisted to Jetpack DataStore Preferences.
5. In UI and Accessibility checks, active lock evaluation is computed as:
   isCurrentlyActive = isActive && System.currentTimeMillis() < endTimeMillis
   emainingMillis = max(0L, endTimeMillis - System.currentTimeMillis())
6. When the device reboots, BootCompletedReceiver receives ndroid.intent.action.BOOT_COMPLETED, inspects the stored timestamps, and reschedules the exact alarm with AlarmManager.setExactAndAllowWhileIdle.

---

## 3. Anti-Impulse Friction Flow

`
User requests "Disable Protection"
              ¦
              ?
Show Current Remaining Duration
              ¦
              ?
Is Friction Configured?
+-- HOLD_BUTTON: Require 5.0 seconds continuous press with radial animation
+-- TYPE_PHRASE: Require typing mindful affirmation without copy-paste
+-- MATH_TASK: Require solving 2-digit mental arithmetic
+-- PIN_CODE: Verify PBKDF2-HMAC-SHA256 salted hash
              ¦
              ? [Completed Successfully]
Unlock Protection & Notify Service
`

---

## 4. Local Persistence Design

- **DataStore Preferences**: Fast, non-blocking asynchronous preferences for:
  - Active lock state, mode, and target timestamp
  - Protection switches (YouTube Shorts, Reels, Spotlight, Adult websites)
  - Friction preferences and hashed PIN
  - App theme and onboarding completion flag
- **SQLite Database (zenith_focus.db)**:
  - Table lock_events: Stores 	imestamp, package_name, category, confidence, ule_id.
  - Indexed by 	imestamp and category for ultra-fast aggregation of daily stats and focus streak calculations.
