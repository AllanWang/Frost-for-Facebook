package com.pitchedapps.frost.web

import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import ca.allanwang.kau.utils.circularReveal
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.isVisible
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Created by Allan Wang on 2017-05-29.
 *
 */
class FrostWebViewCore @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr), NestedScrollingChild {

    private val childHelper = NestedScrollingChildHelper(this)
    private var lastY: Int = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedOffsetY: Int = 0
    val progressObservable: PublishSubject<Int>     // Keeps track of every progress change
    val refreshObservable: PublishSubject<Boolean>  // Only emits on page loads
    val titleObservable: BehaviorSubject<String>    // Only emits on different non http titles


    var baseUrl: String? = null
    var baseEnum: FbTab? = null //only viewpager items should pass the base enum
    internal lateinit var frostWebClient: FrostWebViewClient

    init {
        isNestedScrollingEnabled = true
        progressObservable = PublishSubject.create<Int>()
        refreshObservable = PublishSubject.create<Boolean>()
        titleObservable = BehaviorSubject.create<String>()
    }

    fun loadUrl(url: String?, animate: Boolean) {
        if (url == null) return
        registerTransition(animate)
        super.loadUrl(url)
    }

    fun reload(animate: Boolean) {
        registerTransition(animate)
        super.reload()
    }

    /**
     * Hook onto the refresh observable for one cycle
     * Animate toggles between the fancy ripple and the basic fade
     * The cycle only starts on the first load since there may have been another process when this is registered
     */
    fun registerTransition(animate: Boolean) {
        var dispose: Disposable? = null
        var loading = false
        dispose = refreshObservable.subscribeOn(AndroidSchedulers.mainThread()).subscribe {
            if (it) {
                loading = true
                if (isVisible) fadeOut(duration = 200L)
            } else if (loading) {
                dispose?.dispose()
                if (animate && Prefs.animate) circularReveal(offset = 150L)
                else fadeIn(duration = 100L)
            }
        }
    }

    fun loadBaseUrl(animate: Boolean = true) {
        loadUrl(baseUrl, animate)
    }

    fun addTitleListener(subscriber: (title: String) -> Unit, scheduler: Scheduler = AndroidSchedulers.mainThread()): Disposable
            = titleObservable.observeOn(scheduler).subscribe(subscriber)

    /**
     * Handle nested scrolling against SwipeRecyclerView
     * Courtesy of takahirom
     *
     * https://github.com/takahirom/webview-in-coordinatorlayout/blob/master/app/src/main/java/com/github/takahirom/webview_in_coodinator_layout/NestedWebView.java
     */
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
            ValueAnimator.ofInt(scrollY, 0).apply {
                duration = Math.min(scrollY, 500).toLong()
                interpolator = DecelerateInterpolator()
                addUpdateListener { scrollY = it.animatedValue as Int }
                start()
            }
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