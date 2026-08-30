package com.openlauncher.app.ui.map.here

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.here.sdk.core.Anchor2D
import com.here.sdk.mapview.MapView
import com.openlauncher.app.R
import com.openlauncher.app.ui.map.HereSearchResult
import kotlin.math.roundToInt

private const val PIN_SIZE_DP = 28
private const val PIN_TEXT_SIZE_SP = 20f

class HereSearchPinRenderer(
    private val mapView: MapView
) {

    private val pinnedViews =
        mutableListOf<View>()

    fun showResults(
        results: List<HereSearchResult>
    ) {
        clear()

        results
            .take(5)
            .forEachIndexed { index, result ->
                val view =
                    createPinView(
                        number = index + 1
                    )

                val viewPin =
                    mapView.pinView(
                        view,
                        result.coordinates
                    )

                if (viewPin != null) {
                    viewPin.setAnchorPoint(
                        Anchor2D(
                            0.5,
                            1.0
                        )
                    )

                    pinnedViews.add(
                        view
                    )
                }
            }
    }

    fun clear() {
        pinnedViews.forEach { view ->
            mapView.unpinView(
                view
            )
        }

        pinnedViews.clear()
    }

    private fun createPinView(
        number: Int
    ): TextView {
        val sizePx =
            dpToPx(
                PIN_SIZE_DP
            )

        val backgroundDrawable =
            GradientDrawable().apply {
                shape =
                    GradientDrawable.RECTANGLE

                setColor(
                    Color.rgb(
                        215,
                        232,
                        0
                    )
                )
            }

        return TextView(
            mapView.context
        ).apply {
            text =
                number.toString()

            setTextColor(
                Color.rgb(
                    5,
                    6,
                    0
                )
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                PIN_TEXT_SIZE_SP
            )

            gravity =
                Gravity.CENTER

            typeface =
                ResourcesCompat.getFont(
                    mapView.context,
                    R.font.ibm_vga_9x16
                )
            paint.isFakeBoldText = true
            includeFontPadding =
                false

            background =
                backgroundDrawable

            setWidth(sizePx)
            setHeight(sizePx)

            minimumWidth = sizePx
            minimumHeight = sizePx
        }
    }

    private fun dpToPx(
        valueDp: Int
    ): Int {
        return (
                valueDp *
                        mapView.resources
                            .displayMetrics
                            .density
                )
            .roundToInt()
    }
}