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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.openlauncher.app.MainActivity

class MediaReturnOverlayService : Service() {

    companion object {
        const val EXTRA_MEDIA_PACKAGE =
            "extra_media_package"

        private const val PRIMARY =
            "#D7E800"

        private const val SECONDARY =
            "#8A9300"

        private const val BACKGROUND =
            "#050600"
    }

    private lateinit var windowManager:
            WindowManager

    private val overlayViews =
        mutableListOf<View>()

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

        if (overlayViews.isNotEmpty()) {
            return START_NOT_STICKY
        }

        val packageName =
            intent?.getStringExtra(
                EXTRA_MEDIA_PACKAGE
            )

        showShell()

        return START_NOT_STICKY
    }

    private fun showShell() {
        showFrame()
        showNavButton()
    }

    private fun showTopBar(
        mediaAppName: String
    ) {
        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(16),
                    0,
                    dp(16),
                    0
                )

                background =
                    panelBackground(
                        fillAlpha = 210,
                        border = true
                    )
            }

        val title =
            TextView(this).apply {
                text =
                    mediaAppName.uppercase()

                setTextColor(
                    Color.parseColor(PRIMARY)
                )

                textSize = 16f
                typeface =
                    Typeface.MONOSPACE

                includeFontPadding = false
            }

        val spacer =
            View(this)

        val status =
            TextView(this).apply {
                text = "MEDIA APP  •  ONLINE"

                setTextColor(
                    Color.parseColor(SECONDARY)
                )

                textSize = 9f
                typeface =
                    Typeface.MONOSPACE

                includeFontPadding = false
            }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            spacer,
            LinearLayout.LayoutParams(
                0,
                1,
                1f
            )
        )

        root.addView(
            status
        )

        addOverlay(
            root,
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                dp(44),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                x = 0
                y = 0

                // Important for touch-through on modern Android.
                alpha = 0.78f
            }
        )
    }

    private fun showLeftRail() {
        val rail =
            FrameLayout(this).apply {

                background =
                    panelBackground(
                        fillAlpha = 150,
                        border = true
                    )

                val footer =
                    TextView(context).apply {
                        text =
                            "AW11\nRETRO\nLAUNCHER"

                        setTextColor(
                            Color.parseColor(
                                SECONDARY
                            )
                        )

                        textSize = 8f
                        typeface =
                            Typeface.MONOSPACE

                        gravity =
                            Gravity.START or
                                    Gravity.BOTTOM

                        setPadding(
                            dp(8),
                            0,
                            0,
                            dp(10)
                        )
                    }

                addView(
                    footer,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }

        addOverlay(
            rail,
            WindowManager.LayoutParams(
                dp(64),
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity =
                    Gravity.START or
                            Gravity.TOP

                x = 0
                y = 0

                alpha = 0.78f
            }
        )
    }

    private fun showNavButton() {
        val nav =
            TextView(this).apply {

                text = "◀  NAV"

                setTextColor(
                    Color.parseColor(PRIMARY)
                )

                textSize = 12f

                typeface =
                    Typeface.MONOSPACE

                gravity =
                    Gravity.CENTER

                includeFontPadding = false

                background =
                    GradientDrawable().apply {
                        shape =
                            GradientDrawable.RECTANGLE

                        setColor(
                            Color.parseColor(
                                "#F0050600"
                            )
                        )

                        setStroke(
                            dp(1),
                            Color.parseColor(
                                PRIMARY
                            )
                        )
                    }

                setOnClickListener {
                    returnToLauncher()
                }
            }

        addOverlay(
            nav,
            WindowManager.LayoutParams(
                dp(68),
                dp(52),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {

                gravity =
                    Gravity.START or
                            Gravity.CENTER_VERTICAL

                x = 0
                y = -dp(105)
            }
        )
    }

    private fun addOverlay(
        view: View,
        params: WindowManager.LayoutParams
    ) {
        runCatching {
            windowManager.addView(
                view,
                params
            )

            overlayViews += view
        }.onFailure {
            stopSelf()
        }
    }

    private fun mediaAppName(
        packageName: String?
    ): String {
        if (packageName.isNullOrBlank()) {
            return "MEDIA"
        }

        return runCatching {
            val info =
                packageManager
                    .getApplicationInfo(
                        packageName,
                        0
                    )

            packageManager
                .getApplicationLabel(info)
                .toString()
        }.getOrDefault("MEDIA")
    }

    private fun panelBackground(
        fillAlpha: Int,
        border: Boolean
    ): GradientDrawable =
        GradientDrawable().apply {

            shape =
                GradientDrawable.RECTANGLE

            setColor(
                Color.argb(
                    fillAlpha,
                    5,
                    6,
                    0
                )
            )

            if (border) {
                setStroke(
                    dp(1),
                    Color.parseColor(
                        PRIMARY
                    )
                )
            }
        }

    private fun showFrame() {
        addFrameEdge(
            width = WindowManager.LayoutParams.MATCH_PARENT,
            height = dp(2),
            gravity = Gravity.TOP
        )

        addFrameEdge(
            width = WindowManager.LayoutParams.MATCH_PARENT,
            height = dp(4),
            gravity = Gravity.BOTTOM
        )

        addFrameEdge(
            width = dp(2),
            height = WindowManager.LayoutParams.MATCH_PARENT,
            gravity = Gravity.START
        )

        addFrameEdge(
            width = dp(2),
            height = WindowManager.LayoutParams.MATCH_PARENT,
            gravity = Gravity.END
        )
    }

    private fun addFrameEdge(
        width: Int,
        height: Int,
        gravity: Int,
        offsetY: Int = 0
    ) {
        val view =
            View(this).apply {
                setBackgroundColor(
                    Color.parseColor(PRIMARY)
                )
            }

        addOverlay(
            view,
            WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                this.gravity = gravity
                y = offsetY
            }
        )
    }

    private fun removeOverlayImmediately() {
        overlayViews
            .toList()
            .forEach { view ->
                runCatching {
                    view.visibility = View.GONE

                    windowManager.removeViewImmediate(
                        view
                    )
                }
            }

        overlayViews.clear()
    }

    private fun returnToLauncher() {

        // Remove the shell before RetroLauncher becomes visible.
        removeOverlayImmediately()

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

        removeOverlayImmediately()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    private fun dp(
        value: Int
    ): Int =
        (
                value *
                        resources
                            .displayMetrics
                            .density
                ).toInt()
}