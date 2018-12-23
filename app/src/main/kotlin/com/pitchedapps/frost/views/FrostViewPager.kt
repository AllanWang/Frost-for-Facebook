package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-07-07.
 *
 * Basic override to allow us to control swiping
 */
class FrostViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : androidx.viewpager.widget.ViewPager(context, attrs) {
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