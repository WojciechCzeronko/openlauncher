package com.openlauncher.app.ui.map.here

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.MapView
import kotlin.math.roundToInt

private const val MARKER_SIZE_DP = 32f
private const val MARKER_STROKE_DP = 2f
private const val MARKER_CENTER_DOT_DP = 2.5f

class HereSelectedLocationRenderer(
    private val mapView: MapView
) {
    private var pinnedView: View? = null

    fun show(
        coordinates: GeoCoordinates
    ) {
        clear()

        val view =
            SelectedLocationMarkerView(
                mapView.context
            )

        val viewPin =
            mapView.pinView(
                view,
                coordinates
            )

        if (viewPin != null) {
            viewPin.setAnchorPoint(
                Anchor2D(
                    0.5,
                    0.5
                )
            )

            pinnedView = view
        }
    }

    fun clear() {
        pinnedView?.let { view ->
            mapView.unpinView(view)
        }

        pinnedView = null
    }
}

private class SelectedLocationMarkerView(
    context: Context
) : View(context) {

    private val density =
        resources.displayMetrics.density

    private val markerSizePx =
        (
                MARKER_SIZE_DP *
                        density
                )
            .roundToInt()

    private val strokePaint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            color =
                Color.rgb(
                    215,
                    232,
                    0
                )

            style =
                Paint.Style.STROKE

            strokeWidth =
                MARKER_STROKE_DP *
                        density

            strokeCap =
                Paint.Cap.SQUARE
        }

    private val centerPaint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            color =
                Color.rgb(
                    215,
                    232,
                    0
                )

            style =
                Paint.Style.FILL
        }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        setMeasuredDimension(
            markerSizePx,
            markerSizePx
        )
    }

    override fun onDraw(
        canvas: Canvas
    ) {
        super.onDraw(canvas)

        val center =
            markerSizePx / 2f

        val radius =
            markerSizePx * 0.25f

        val gap =
            3f * density

        canvas.drawCircle(
            center,
            center,
            radius,
            strokePaint
        )

        canvas.drawLine(
            center,
            0f,
            center,
            center - radius - gap,
            strokePaint
        )

        canvas.drawLine(
            center,
            center + radius + gap,
            center,
            markerSizePx.toFloat(),
            strokePaint
        )

        canvas.drawLine(
            0f,
            center,
            center - radius - gap,
            center,
            strokePaint
        )

        canvas.drawLine(
            center + radius + gap,
            center,
            markerSizePx.toFloat(),
            center,
            strokePaint
        )

        canvas.drawCircle(
            center,
            center,
            MARKER_CENTER_DOT_DP *
                    density,
            centerPaint
        )
    }
}