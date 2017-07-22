package com.pitchedapps.frost.views

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-07-07.
 *
 * Basic override to allow us to control swiping
 */
class FrostViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {
    var enableSwipe = true

    override fun onInterceptTouchEvent(ev: MotionEvent?) = Prefs.viewpagerSwipe && enableSwipe && super.onInterceptTouchEvent(ev)

    override fun onTouchEvent(ev: MotionEvent?): Boolean = Prefs.viewpagerSwipe && enableSwipe && super.onTouchEvent(ev)
}