package com.pitchedapps.frost.web

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Allan Wang on 2017-05-29.
 *
 * Courtesy of takahirom
 *
 * https://github.com/takahirom/webview-in-coordinatorlayout/blob/master/app/src/main/java/com/github/takahirom/webview_in_coodinator_layout/NestedWebView.java
 */
class FrostWebViewCore @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr), NestedScrollingChild {

    private val childHelper = NestedScrollingChildHelper(this)
    private var lastY: Int = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedOffsetY: Int = 0
    val progressObservable: BehaviorSubject<Int>    // Keeps track of every progress change
    val refreshObservable: BehaviorSubject<Boolean> // Only emits on page loads
    val titleObservable: BehaviorSubject<String>    // Only emits on different non http titles

    var baseUrl: String? = null
    var baseEnum: FbTab? = null
    internal var frostWebClient: FrostWebViewClient? = null

    init {
        isNestedScrollingEnabled = true
        progressObservable = BehaviorSubject.create<Int>()
        refreshObservable = BehaviorSubject.create<Boolean>()
        titleObservable = BehaviorSubject.create<String>()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview(url: String, enum: FbTab? = null) {
        baseUrl = url
        baseEnum = enum
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
//        settings.domStorageEnabled = true
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        frostWebClient = baseEnum?.webClient?.invoke(refreshObservable) ?: FrostWebViewClient(refreshObservable)
        webViewClient = frostWebClient
        webChromeClient = FrostChromeClient(progressObservable, titleObservable)
        addJavascriptInterface(FrostJSI(context, this), "Frost")
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun loadUrl(url: String?) {
        if (url != null)
            super.loadUrl(url)
    }

    fun loadBaseUrl() {
        loadUrl(baseUrl)
    }

    fun addTitleListener(subscriber: (title: String) -> Unit, scheduler: Scheduler = AndroidSchedulers.mainThread()): Disposable
            = titleObservable.observeOn(scheduler).subscribe(subscriber)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
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

    /**
     * If webview is already at the top, refresh
     * Otherwise scroll to top
     */
    fun scrollOrRefresh() {
        if (scrollY < 5) loadBaseUrl()
        else scrollToTop()
    }

    fun scrollToTop() {
        flingScroll(0, 0) // stop fling
        if (scrollY > 10000) {
            scrollTo(0, 0)
        } else {
            val animator = ValueAnimator.ofInt(scrollY, 0)
            animator.duration = Math.min(scrollY, 500).toLong()
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener { scrollY = it.animatedValue as Int }
            animator.start()
        }
    }

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