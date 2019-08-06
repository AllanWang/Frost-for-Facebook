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

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.webkit.WebView
import android.widget.ListView
import androidx.core.widget.ListViewCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnChildScrollUpCallback
import com.pitchedapps.frost.utils.L

/**
 * Variant that forbids refreshing if child layout is not at the top
 * Inspired by https://github.com/slapperwan/gh4a/blob/master/app/src/main/java/com/gh4a/widget/SwipeRefreshLayout.java
 *
 */
class SwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SwipeRefreshLayout(context, attrs) {

    private var preventRefresh: Boolean = false
    private var downY: Float = -1f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var nestedCanChildScrollUp: OnChildScrollUpCallback? = null

    /**
     * Copy of [canChildScrollUp], with additional support if necessary
     */
    private val canChildScrollUp = OnChildScrollUpCallback { parent, child ->
        nestedCanChildScrollUp?.canChildScrollUp(parent, child) ?: when (child) {
            is WebView -> child.canScrollVertically(-1).apply {
                L.d { "Webview can scroll up $this" }
            }
            is ListView -> ListViewCompat.canScrollList(child, -1)
            // Supports webviews as well
            else -> child?.canScrollVertically(-1) ?: false
        }
    }

    init {
        setOnChildScrollUpCallback(canChildScrollUp)
    }

    override fun setOnChildScrollUpCallback(callback: OnChildScrollUpCallback?) {
        this.nestedCanChildScrollUp = callback
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_DOWN && preventRefresh) {
            return false
        }
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downY = ev.y
                preventRefresh = canChildScrollUp()
            }
            MotionEvent.ACTION_MOVE -> {
                if (downY - ev.y > touchSlop) {
                    preventRefresh = true
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        if (preventRefresh) {
            /*
             * Ignoring offsetInWindow since
             * 1. It doesn't seem to matter in the typical use case
             * 2. It isn't being transferred to the underlying array used by the super class
             */
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null)
        } else {
            super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        }
    }

    /**
     * Alias for adding on refresh listener
     */
    interface OnRefreshListener : SwipeRefreshLayout.OnRefreshListener
}
