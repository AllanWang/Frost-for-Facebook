package com.pitchedapps.frost.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by Allan Wang on 2016-11-17.
 *
 *
 * Canvas drawn ripples that keep the previous color
 * Extends to view dimensions
 * Supports multiple ripples from varying locations
 */
class RippleCanvas @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val paint: Paint = Paint()
    var baseColor = Color.TRANSPARENT
    val ripples: MutableList<Ripple> = mutableListOf()

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(baseColor)
        val itr = ripples.iterator()
        while (itr.hasNext()) {
            val r = itr.next()
            paint.color = r.color
            canvas.drawCircle(r.x, r.y, r.radius, paint)
            if (r.radius == r.maxRadius) {
                itr.remove()
                baseColor = r.color
            }
        }
    }

    @JvmOverloads fun ripple(color: Int, startX: Float = 0f, startY: Float = 0f, duration: Int = 1000) {
        var x = startX
        var y = startY
        val w = width.toFloat()
        val h = height.toFloat()
        if (x == MIDDLE)
            x = w / 2
        else if (x > w) x = 0f
        if (y == MIDDLE)
            y = h / 2
        else if (y > h) y = 0f
        val maxRadius = Math.hypot(Math.max(x, w - x).toDouble(), Math.max(y, h - y).toDouble()).toFloat()
        val ripple = Ripple(color, x, y, 0f, maxRadius)
        ripples.add(ripple)
        val animator = ValueAnimator.ofFloat(0f, maxRadius)
        animator.duration = duration.toLong()
        animator.addUpdateListener { animation ->
            ripple.setRadius(animation.animatedValue as Float)
            invalidate()
        }
        animator.start()
    }

    fun set(color: Int) {
        baseColor = color
        ripples.clear()
        invalidate()
    }

    inner class Ripple internal constructor(val color: Int, val x: Float, val y: Float, var radius: Float, val maxRadius: Float) {
        internal fun setRadius(r: Float) {
            radius = r
        }
    }

    companion object {
        val MIDDLE = -1.0f
    }
}
