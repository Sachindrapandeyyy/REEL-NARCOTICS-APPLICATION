package com.zenith.focus.accessibility.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Lightweight, lifecycle-safe physical click shield.
 * Overlays an invisible, non-focusable touch-absorbing view directly on top of
 * short-form bottom navigation tabs (Shorts, Reels, Spotlight) during active locks.
 */
class ShortFormButtonShield(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var shieldView: View? = null
    private var currentBounds: Rect? = null
    private var currentTargetPackage: String? = null

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    fun updateShield(packageName: String, tabBounds: Rect?, isLockActive: Boolean) {
        runOnMainThread {
            if (!isLockActive || tabBounds == null || !Settings.canDrawOverlays(context)) {
                removeShieldInternal()
                return@runOnMainThread
            }

            val lowerPkg = packageName.lowercase(java.util.Locale.US)
            val isTargetApp = lowerPkg.contains("youtube") ||
                    lowerPkg.contains("instagram") ||
                    lowerPkg.contains("facebook") ||
                    lowerPkg.contains("snapchat")

            if (!isTargetApp || tabBounds.isEmpty || tabBounds.width() <= 0 || tabBounds.height() <= 0) {
                removeShieldInternal()
                return@runOnMainThread
            }

            if (shieldView == null) {
                attachShieldInternal(tabBounds, lowerPkg)
            } else if (currentBounds != tabBounds || currentTargetPackage != lowerPkg) {
                updateShieldLayoutInternal(tabBounds, lowerPkg)
            }
        }
    }

    private fun attachShieldInternal(bounds: Rect, packageName: String) {
        val wm = windowManager ?: return
        val view = View(context).apply {
            // Faint 0x01 alpha background ensures hardware touch interception while remaining visually unnoticeable
            setBackgroundColor(0x01000000)
            setOnClickListener {
                triggerHaptic()
            }
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bounds.left
            y = bounds.top
        }

        try {
            wm.addView(view, params)
            shieldView = view
            currentBounds = Rect(bounds)
            currentTargetPackage = packageName
        } catch (e: WindowManager.BadTokenException) {
            android.util.Log.w("ShortFormButtonShield", "BadTokenException adding shield overlay: ${e.message}")
            cleanupState()
        } catch (e: SecurityException) {
            android.util.Log.w("ShortFormButtonShield", "SecurityException adding shield overlay: ${e.message}")
            cleanupState()
        } catch (e: IllegalArgumentException) {
            android.util.Log.w("ShortFormButtonShield", "IllegalArgumentException adding shield overlay: ${e.message}")
            cleanupState()
        } catch (e: Exception) {
            android.util.Log.w("ShortFormButtonShield", "Failed to attach shield view: ${e.message}")
            cleanupState()
        }
    }

    private fun updateShieldLayoutInternal(bounds: Rect, packageName: String) {
        val wm = windowManager ?: return
        val view = shieldView ?: return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bounds.left
            y = bounds.top
        }

        try {
            wm.updateViewLayout(view, params)
            currentBounds = Rect(bounds)
            currentTargetPackage = packageName
        } catch (e: WindowManager.BadTokenException) {
            android.util.Log.w("ShortFormButtonShield", "BadTokenException updating layout: ${e.message}")
            removeShieldInternal()
        } catch (e: SecurityException) {
            android.util.Log.w("ShortFormButtonShield", "SecurityException updating layout: ${e.message}")
            removeShieldInternal()
        } catch (e: IllegalArgumentException) {
            android.util.Log.w("ShortFormButtonShield", "IllegalArgumentException updating layout: ${e.message}")
            removeShieldInternal()
        } catch (e: Exception) {
            android.util.Log.w("ShortFormButtonShield", "Failed updating shield layout: ${e.message}")
            removeShieldInternal()
        }
    }

    fun removeShield() {
        runOnMainThread {
            removeShieldInternal()
        }
    }

    private fun removeShieldInternal() {
        val wm = windowManager ?: return
        val view = shieldView ?: return
        try {
            wm.removeViewImmediate(view)
        } catch (_: WindowManager.BadTokenException) {
        } catch (_: IllegalArgumentException) {
            // Thrown if view was not attached or already detached
        } catch (_: Exception) {
            try {
                wm.removeView(view)
            } catch (_: Exception) {}
        } finally {
            cleanupState()
        }
    }

    private fun cleanupState() {
        shieldView = null
        currentBounds = null
        currentTargetPackage = null
    }

    private fun triggerHaptic() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(VibrationEffect.createOneShot(40L, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            Unit
        }
    }
}
