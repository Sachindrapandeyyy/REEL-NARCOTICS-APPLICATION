package com.zenith.focus.feature.psychology

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * A subtle, non-intrusive 2.5dp progress bar at the very top of the screen
 * that visually communicates the remaining time in an intentional time-boxed session.
 * Flagged NOT_TOUCHABLE so it never intercepts user interaction.
 */
class TimeBoxProgressShield(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var barView: ProgressBarView? = null
    private var isAttached = false

    private class ProgressBarView(context: Context) : View(context) {
        var progress: Float = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
                invalidate()
            }

        private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33000000 // Subtle 20% dark background
        }

        private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF00E676.toInt() // Vibrant focus emerald
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0 || h <= 0) return

            // Draw track
            canvas.drawRect(0f, 0f, w, h, backgroundPaint)

            // Dynamic color shift: emerald -> amber -> coral when reaching final 20%
            if (progress > 0.8f) {
                progressPaint.color = 0xFFFF5252.toInt() // Coral alert
            } else if (progress > 0.6f) {
                progressPaint.color = 0xFFFFB300.toInt() // Amber caution
            } else {
                progressPaint.color = 0xFF00E676.toInt() // Emerald calm
            }

            // Draw progress fill (elapsed percentage)
            val fillWidth = w * progress
            canvas.drawRect(0f, 0f, fillWidth, h, progressPaint)
        }
    }

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    fun show(progressFraction: Float) {
        runOnMainThread {
            if (!Settings.canDrawOverlays(context)) {
                removeInternal()
                return@runOnMainThread
            }

            val wm = windowManager ?: return@runOnMainThread

            if (barView == null) {
                barView = ProgressBarView(context)
            }

            val view = barView ?: return@runOnMainThread
            view.progress = progressFraction

            if (!isAttached) {
                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val barHeightPx = (3f * context.resources.displayMetrics.density).toInt().coerceAtLeast(4)

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    barHeightPx,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = 0
                    y = 0
                }

                try {
                    wm.addView(view, params)
                    isAttached = true
                } catch (e: WindowManager.BadTokenException) {
                    android.util.Log.w("TimeBoxProgressShield", "BadTokenException attaching: ${e.message}")
                    cleanupState()
                } catch (e: SecurityException) {
                    android.util.Log.w("TimeBoxProgressShield", "SecurityException attaching: ${e.message}")
                    cleanupState()
                } catch (e: IllegalArgumentException) {
                    android.util.Log.w("TimeBoxProgressShield", "IllegalArgumentException attaching: ${e.message}")
                    cleanupState()
                } catch (e: Exception) {
                    android.util.Log.w("TimeBoxProgressShield", "Failed attaching progress shield: ${e.message}")
                    cleanupState()
                }
            } else {
                view.requestLayout()
                view.invalidate()
            }
        }
    }

    fun remove() {
        runOnMainThread {
            removeInternal()
        }
    }

    private fun removeInternal() {
        val wm = windowManager ?: return
        val view = barView ?: return
        if (!isAttached) return

        try {
            wm.removeViewImmediate(view)
        } catch (_: WindowManager.BadTokenException) {
        } catch (_: IllegalArgumentException) {
        } catch (_: Exception) {
            try {
                wm.removeView(view)
            } catch (_: Exception) {}
        } finally {
            cleanupState()
        }
    }

    private fun cleanupState() {
        barView = null
        isAttached = false
    }
}
