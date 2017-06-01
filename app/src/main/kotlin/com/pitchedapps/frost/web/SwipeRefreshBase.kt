package com.pitchedapps.frost.web

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import com.pitchedapps.frost.utils.Utils


/**
 * Created by Allan Wang on 2017-05-28.
 */
class SwipeRefreshBase @JvmOverloads constructor(
        context: Context?, attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    lateinit var shouldSwipe: (ev: MotionEvent) -> Boolean

    companion object {
        private val SCROLL_BUFFER by lazy { Utils.dpToPx(5) }
        fun shouldScroll(webview: WebView) = webview.scrollY <= SCROLL_BUFFER
    }

//    override fun onInterceptTouchEvent(ev: MotionEvent):Boolean {
//        val b = shouldSwipe.invoke(ev) && super.onInterceptTouchEvent(ev)
//        L.e("Should swipe $b")
//        return b
//    }
}