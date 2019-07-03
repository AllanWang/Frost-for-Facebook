/*
 * Copyright 2019 Allan Wang
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
package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper

class DragFrame @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var dragHelper: ViewDragHelper? = null
    var viewToIgnore: View? = null
    private val rect = Rect()
    private val location = IntArray(2)
    private var shouldIgnore: Boolean = false

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            shouldIgnore = shouldIgnore(event)
        }
        if (shouldIgnore) {
            return false
        }
        return try {
            dragHelper?.shouldInterceptTouchEvent(event) ?: false
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            shouldIgnore = shouldIgnore(event)
        }
        if (shouldIgnore) {
            return false
        }
        try {
            dragHelper?.processTouchEvent(event) ?: return false
        } catch (e: Exception) {
            return false
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            shouldIgnore = shouldIgnore(event)
        }
        if (shouldIgnore) {
            return false
        }
        return super.dispatchTouchEvent(event)
    }

    private fun shouldIgnore(event: MotionEvent): Boolean {
        val v = viewToIgnore ?: return false
        v.getDrawingRect(rect)
        v.getLocationOnScreen(location)
        rect.offset(location[0], location[1])
        return rect.contains(event.rawX.toInt(), event.rawY.toInt())
    }

    override fun computeScroll() {
        super.computeScroll()
        if (dragHelper?.continueSettling(true) == true) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }
}
