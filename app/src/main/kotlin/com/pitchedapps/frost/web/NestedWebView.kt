/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat

/**
 * Created by Allan Wang on 20/12/17.
 *
 * Webview extension that handles nested scrolls
 */
open class NestedWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr), NestedScrollingChild {

    private lateinit var childHelper: NestedScrollingChildHelper
    private var lastY: Int = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedOffsetY: Int = 0

    init {
        init()
    }

    fun init() {
        // To avoid leaking constructor
        childHelper = NestedScrollingChildHelper(this)
    }

    /**
     * Handle nested scrolling against SwipeRecyclerView
     * Courtesy of takahirom
     *
     * https://github.com/takahirom/webview-in-coordinatorlayout/blob/master/app/src/main/java/com/github/takahirom/webview_in_coodinator_layout/NestedWebView.java
     */
    @SuppressLint("ClickableViewAccessibility")
    final override fun onTouchEvent(ev: MotionEvent): Boolean {
        val event = MotionEvent.obtain(ev)
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN)
            nestedOffsetY = 0
        val eventY = event.y.toInt()
        event.offsetLocation(0f, nestedOffsetY.toFloat())
        val returnValue: Boolean
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                var deltaY = lastY - eventY
                // NestedPreScroll
                if (dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1]
                }
                lastY = eventY - scrollOffset[1]
                returnValue = super.onTouchEvent(event)
                // NestedScroll
                if (dispatchNestedScroll(0, scrollOffset[1], 0, deltaY, scrollOffset)) {
                    event.offsetLocation(0f, scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
                    lastY -= scrollOffset[1]
                }
            }
            MotionEvent.ACTION_DOWN -> {
                returnValue = super.onTouchEvent(event)
                lastY = eventY
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                returnValue = super.onTouchEvent(event)
                stopNestedScroll()
            }
            else -> return false
        }
        return returnValue
    }

    /*
     * ---------------------------------------------
     * Nested Scrolling Content
     * ---------------------------------------------
     */

    final override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    final override fun isNestedScrollingEnabled() = childHelper.isNestedScrollingEnabled

    final override fun startNestedScroll(axes: Int) = childHelper.startNestedScroll(axes)

    final override fun stopNestedScroll() = childHelper.stopNestedScroll()

    final override fun hasNestedScrollingParent() = childHelper.hasNestedScrollingParent()

    final override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ) = childHelper.dispatchNestedScroll(
        dxConsumed,
        dyConsumed,
        dxUnconsumed,
        dyUnconsumed,
        offsetInWindow
    )

    final override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ) = childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)

    final override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean) =
        childHelper.dispatchNestedFling(velocityX, velocityY, consumed)

    final override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float) =
        childHelper.dispatchNestedPreFling(velocityX, velocityY)
}
