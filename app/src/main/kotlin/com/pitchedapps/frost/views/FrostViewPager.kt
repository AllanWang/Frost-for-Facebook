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
package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-07-07.
 *
 * Basic override to allow us to control swiping
 */
class FrostViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewPager(context, attrs) {
    var enableSwipe = true

    override fun onInterceptTouchEvent(ev: MotionEvent?) =
        try {
            Prefs.viewpagerSwipe && enableSwipe && super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            false
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean =
        try {
            Prefs.viewpagerSwipe && enableSwipe && super.onTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            false
        }
}
