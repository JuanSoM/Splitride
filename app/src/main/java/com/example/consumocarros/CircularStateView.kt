package com.example.consumocarros

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class CircularStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var stateValue: Int = 0   // valor actual
    private var animator: ValueAnimator? = null  // animador activo

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    /** Getter opcional */
    fun getState(): Int = stateValue

    // Para compatibilidad con Java
    fun setState(value: Int) {
        setState(value, true)
    }


    /** ANIMACIÓN automática en cada cambio */
    fun setState(value: Int, animate: Boolean = true) {
        val finalValue = value.coerceIn(0, 100)

        // Cancelar animación previa si existe
        animator?.cancel()

        if (animate) {
            animator = ValueAnimator.ofInt(stateValue, finalValue).apply {
                duration = 800
                addUpdateListener {
                    stateValue = it.animatedValue as Int
                    invalidate()
                }
                start()
            }
        } else {
            stateValue = finalValue
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height)
        val radius = size / 2f - paint.strokeWidth

        val centerX = width / 2f
        val centerY = height / 2f

        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Convertir progreso a ángulo
        val sweepAngle = (stateValue / 100f) * 360f

        // Color interpolado entre rojo y verde
        val color = interpolateColor(Color.RED, Color.GREEN, stateValue / 100f)
        paint.color = color

        canvas.drawArc(rect, -90f, sweepAngle, false, paint)
    }

    private fun interpolateColor(colorStart: Int, colorEnd: Int, fraction: Float): Int {
        val r = Color.red(colorStart) + ((Color.red(colorEnd) - Color.red(colorStart)) * fraction).toInt()
        val g = Color.green(colorStart) + ((Color.green(colorEnd) - Color.green(colorStart)) * fraction).toInt()
        val b = Color.blue(colorStart) + ((Color.blue(colorEnd) - Color.blue(colorStart)) * fraction).toInt()
        return Color.rgb(r, g, b)
    }
}
