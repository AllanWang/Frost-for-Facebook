package com.pitchedapps.frost.intro

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.setPaddingHorizontal
import ca.allanwang.kau.utils.setPaddingVertical

/**
 * Created by Allan Wang on 2017-07-27.
 */
class IntroPhoneContainer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private fun strokePaint(color: Number) = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        this.color = color.toInt()
    }

    private fun fillPaint(color: Number) = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        this.color = color.toInt()
    }

    val whitePaint = fillPaint(Color.WHITE)

    val bluePaint = fillPaint(0xff3b5998)

    val tabPaint = fillPaint(0xff323232)

    val eraser: Paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = Color.TRANSPARENT
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        }
    }

    val phoneRect = RectF()
    val topTabRect = RectF()
    val phoneStroke = 10f.dpToPx
    val phoneRadius = 15f.dpToPx
    val tabHeight = 56f.dpToPx

    init {
        setWillNotDraw(false)
        alpha = 0f
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, phoneRect.left, canvas.height.toFloat(), bluePaint)
        canvas.drawRect(phoneRect.right, 0f, canvas.width.toFloat(), canvas.height.toFloat(), bluePaint)
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), phoneRect.top, bluePaint)
        canvas.drawRect(0f, phoneRect.bottom, canvas.width.toFloat(), canvas.height.toFloat(), bluePaint)
        val portrait = canvas.height > canvas.width
        val px = if (portrait) phoneRadius else 3 * phoneStroke
        val py = if (portrait) 3 * phoneStroke else phoneRadius
        canvas.drawPhoneEdge(phoneRect.left - px, phoneRect.top - py, phoneRect.right + px, phoneRect.top)
        canvas.drawRect(phoneRect.left - px, phoneRect.top - py / 2, phoneRect.left, phoneRect.bottom + py / 2, whitePaint)
        canvas.drawRect(phoneRect.right, phoneRect.top - py / 2, phoneRect.right + px, phoneRect.bottom + py / 2, whitePaint)
        canvas.drawPhoneEdge(phoneRect.left - px, phoneRect.bottom, phoneRect.right + px, phoneRect.bottom + py)

        canvas.drawRect(topTabRect, tabPaint)
        super.onDraw(canvas)
    }

    private fun Canvas.drawPhoneEdge(left: Float, top: Float, right: Float, bottom: Float)
            = drawRoundRect(left, top, right, bottom, phoneRadius, phoneRadius, whitePaint)


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val border = Math.max(w, h).toFloat() / 20f
        setPaddingHorizontal((border + phoneStroke / 2).toInt())
        setPaddingVertical((border + phoneStroke / 2 + tabHeight).toInt())
        phoneRect.set(border, border, w - border, h - border)
        topTabRect.set(border, border, w - border, border + phoneStroke / 2 + tabHeight)
        super.onSizeChanged(w, h, oldw, oldh)
    }
}