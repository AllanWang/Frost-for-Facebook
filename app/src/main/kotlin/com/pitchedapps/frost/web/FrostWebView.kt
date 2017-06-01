package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.MotionEventCompat
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import com.pitchedapps.frost.events.WebEvent
import com.pitchedapps.frost.utils.ObservableContainer
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

enum class WebStatus {
    LOADING, LOADED, ERROR
}

/**
 * Created by Allan Wang on 2017-05-29.
 *
 * Courtesy of takahirom
 *
 * https://github.com/takahirom/webview-in-coordinatorlayout/blob/master/app/src/main/java/com/github/takahirom/webview_in_coodinator_layout/NestedWebView.java
 */
class FrostWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr), NestedScrollingChild, ObservableContainer<WebStatus> {

    private val childHelper = NestedScrollingChildHelper(this)
    private var lastY: Int = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedOffsetY: Int = 0
    override val observable: Subject<WebStatus> //TODO see if we need this
    var baseUrl: String? = null

    init {
        isNestedScrollingEnabled = true
        observable = BehaviorSubject.create<WebStatus>()
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        setWebViewClient(FrostWebViewClient(observable))
    }

    override fun loadUrl(url: String?) {
        if (url != null)
            super.loadUrl(url)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val event = MotionEvent.obtain(ev)
        val action = MotionEventCompat.getActionMasked(event)
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
                    event.offsetLocation(0f, -scrollOffset[1].toFloat())
                    nestedOffsetY += scrollOffset[1]
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this);
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    @Subscribe
    fun webEvent(event: WebEvent) = event.execute(this)

    // Nested Scroll implements
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled() = childHelper.isNestedScrollingEnabled

    override fun startNestedScroll(axes: Int) = childHelper.startNestedScroll(axes)

    override fun stopNestedScroll() = childHelper.stopNestedScroll()

    override fun hasNestedScrollingParent() = childHelper.hasNestedScrollingParent()

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?)
            = childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?)
            = childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean)
            = childHelper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float)
            = childHelper.dispatchNestedPreFling(velocityX, velocityY)

}