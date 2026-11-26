package com.zenith.focus.accessibility.overlay

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
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
import java.util.Locale

/**
 * Lightweight, lifecycle-safe physical click shield.
 * Overlays a touch-absorbing view directly on top of short-form bottom navigation tabs (Shorts, Reels, Spotlight).
 * In Stealth Cover mode, seamlessly masks the icon using app-matching dark/light background colors
 * with a subtle muted dot indicator (25% alpha).
 */
class ShortFormButtonShield(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var shieldView: ShieldCoverView? = null
    private var currentBounds: Rect? = null
    private var currentTargetPackage: String? = null
    private var currentStealthMode: Boolean = true
    private var lastHapticTimeMs: Long = 0L

    private class ShieldCoverView(context: Context) : View(context) {
        var coverColor: Int = 0x01000000
        var dotColor: Int = 0x00000000
        var isStealthCover: Boolean = true

        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            if (isStealthCover) {
                canvas.drawColor(coverColor)
                if (dotColor != 0) {
                    dotPaint.color = dotColor
                    val cx = width / 2f
                    val cy = height / 2f
                    val radius = 3.5f * resources.displayMetrics.density
                    canvas.drawCircle(cx, cy, radius, dotPaint)
                }
            } else {
                super.onDraw(canvas)
            }
        }
    }

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    fun updateShield(
        packageName: String,
        tabBounds: Rect?,
        isLockActive: Boolean,
        stealthCoverEnabled: Boolean = true
    ) {
        runOnMainThread {
            if (!isLockActive || tabBounds == null || !Settings.canDrawOverlays(context)) {
                removeShieldInternal()
                return@runOnMainThread
            }

            val lowerPkg = packageName.lowercase(Locale.US)
            val isTargetApp = lowerPkg.contains("youtube") ||
                    lowerPkg.contains("instagram") ||
                    lowerPkg.contains("facebook") ||
                    lowerPkg.contains("snapchat")

            val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            if (isLandscape || !isTargetApp || tabBounds.isEmpty || tabBounds.width() <= 0 || tabBounds.height() <= 0) {
                removeShieldInternal()
                return@runOnMainThread
            }

            if (shieldView == null) {
                attachShieldInternal(tabBounds, lowerPkg, stealthCoverEnabled)
            } else if (currentBounds != tabBounds || currentTargetPackage != lowerPkg || currentStealthMode != stealthCoverEnabled) {
                updateShieldLayoutInternal(tabBounds, lowerPkg, stealthCoverEnabled)
            }
        }
    }

    private fun isDarkMode(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun configureShieldStyling(view: ShieldCoverView, packageName: String, stealthCoverEnabled: Boolean) {
        view.isStealthCover = stealthCoverEnabled
        if (stealthCoverEnabled) {
            val dark = isDarkMode()
            view.coverColor = ShortFormButtonShieldTheme.getBackgroundColor(packageName, dark)
            view.dotColor = ShortFormButtonShieldTheme.getDotColor(dark)
            view.setBackgroundColor(0)
        } else {
            view.setBackgroundColor(0x01000000)
        }
        view.invalidate()
    }

    private fun attachShieldInternal(bounds: Rect, packageName: String, stealthCoverEnabled: Boolean) {
        val wm = windowManager ?: return
        val view = ShieldCoverView(context).apply {
            setOnClickListener {
                triggerHaptic()
            }
        }
        configureShieldStyling(view, packageName, stealthCoverEnabled)

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
            currentStealthMode = stealthCoverEnabled
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

    private fun updateShieldLayoutInternal(bounds: Rect, packageName: String, stealthCoverEnabled: Boolean) {
        val wm = windowManager ?: return
        val view = shieldView ?: return

        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            removeShieldInternal()
            return
        }

        configureShieldStyling(view, packageName, stealthCoverEnabled)

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
            currentStealthMode = stealthCoverEnabled
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
        currentStealthMode = true
    }

    private fun triggerHaptic() {
        val now = android.os.SystemClock.elapsedRealtime()
        if (lastHapticTimeMs > 0L && now - lastHapticTimeMs < 350L) return
        lastHapticTimeMs = now
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
