package com.openlauncher.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.openlauncher.app.MainActivity

class MediaReturnOverlayService : Service() {

    private lateinit var windowManager: WindowManager

    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()

        windowManager =
            getSystemService(
                Context.WINDOW_SERVICE
            ) as WindowManager
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (overlayView == null) {
            showOverlay()
        }

        return START_NOT_STICKY
    }

    private fun showOverlay() {

        val view =
            TextView(this).apply {

                text = "◀ NAV"

                setTextColor(
                    Color.parseColor("#D7E800")
                )

                textSize = 11f

                typeface = Typeface.MONOSPACE

                gravity = Gravity.CENTER

                includeFontPadding = false

                background =
                    GradientDrawable().apply {

                        shape =
                            GradientDrawable.RECTANGLE

                        setColor(
                            Color.parseColor("#F0050600")
                        )

                        setStroke(
                            dp(1),
                            Color.parseColor("#D7E800")
                        )
                    }

                setOnClickListener {
                    returnToLauncher()
                }
            }

        val params =
            WindowManager.LayoutParams(
                dp(76),
                dp(44),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {

                gravity =
                    Gravity.START or
                            Gravity.CENTER_VERTICAL

                x = dp(8)

                y = 0
            }

        runCatching {

            windowManager.addView(
                view,
                params
            )

            overlayView = view

        }.onFailure {

            stopSelf()
        }
    }

    private fun returnToLauncher() {

        val intent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        startActivity(intent)

        stopSelf()
    }

    override fun onDestroy() {

        overlayView?.let { view ->

            runCatching {
                windowManager.removeView(view)
            }
        }

        overlayView = null

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    private fun dp(value: Int): Int =
        (
                value *
                        resources.displayMetrics.density
                ).toInt()
}