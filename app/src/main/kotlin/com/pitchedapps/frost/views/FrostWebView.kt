package com.pitchedapps.frost.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import ca.allanwang.kau.utils.circularReveal
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.isVisible
import com.pitchedapps.frost.contracts.FrostUrlData
import com.pitchedapps.frost.contracts.FrostViewContract
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostDownload
import com.pitchedapps.frost.web.*
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Created by Allan Wang on 2017-05-29.
 *
 */
class FrostWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr), NestedScrollingChild, FrostViewContract {

    override fun reload(animate: Boolean) {
        registerTransition(animate)
        super.reload()
    }

    override val view: View = this

    private val childHelper = NestedScrollingChildHelper(this)
    private var lastY: Int = 0
    private val scrollOffset = IntArray(2)
    private val scrollConsumed = IntArray(2)
    private var nestedOffsetY: Int = 0

    override var baseUrl: String = ""
    override var baseEnum: FbItem? = null //only viewpager items should pass the base enum
    override lateinit var progressObservable: PublishSubject<Int>
    override lateinit var refreshObservable: PublishSubject<Boolean>
    override lateinit var titleObservable: BehaviorSubject<String>

    internal lateinit var frostWebClient: FrostWebViewClient

    @SuppressLint("SetJavaScriptEnabled")
    override fun init(dataContract: FrostUrlData) {
        with(settings) {
            javaScriptEnabled = true
            if (url.shouldUseBasicAgent)
                userAgentString = com.pitchedapps.frost.facebook.USER_AGENT_BASIC
            allowFileAccess = true
            textZoom = com.pitchedapps.frost.utils.Prefs.webTextScaling
        }
        setLayerType(LAYER_TYPE_HARDWARE, null)
        // attempt to get custom client; otherwise fallback to original
        frostWebClient = (dataContract as? WebFragment)?.client(this) ?: FrostWebViewClient(this)
        webViewClient = frostWebClient
        webChromeClient = FrostChromeClient(this)
        addJavascriptInterface(FrostJSI(this), "Frost")
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        setDownloadListener(context::frostDownload)
    }


    /**
     * Wrapper to the main userAgentString to cache it.
     * This decouples it from the UiThread
     *
     * Note that this defaults to null, but the main purpose is to
     * check if we've set our own agent.
     *
     * A null value may be interpreted as the default value
     */
    var userAgentString: String? = null
        set(value) {
            field = value
            settings.userAgentString = value
        }

    init {
        isNestedScrollingEnabled = true
    }

    fun loadUrl(url: String?, animate: Boolean) {
        if (url == null) return
        registerTransition(animate)
        super.loadUrl(url)
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
                if (animate && Prefs.animate) circularReveal(offset = WEB_LOAD_DELAY)
                else fadeIn(duration = 100L)
            }
        }
    }

    override fun reloadBase(animate: Boolean) {
        loadUrl(baseUrl, animate)
    }

    fun addTitleListener(subscriber: (title: String) -> Unit, scheduler: Scheduler = AndroidSchedulers.mainThread()): Disposable
            = titleObservable.observeOn(scheduler).subscribe(subscriber)

    override fun onBackPressed(): Boolean {
        if (canGoBack()) {
            goBack()
            return true
        }
        return false
    }


    /**
     * Handle nested scrolling against SwipeRecyclerView
     * Courtesy of takahirom
     *
     * https://github.com/takahirom/webview-in-coordinatorlayout/blob/master/app/src/main/java/com/github/takahirom/webview_in_coodinator_layout/NestedWebView.java
     */
    @SuppressLint("ClickableViewAccessibility")
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
    override fun scrollOrRefresh() {
        if (scrollY < 5) reloadBase(true)
        else scrollToTop()
    }

    private fun scrollToTop() {
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

    override fun reloadTextSize() {
        settings.textZoom = Prefs.webTextScaling
    }

    override fun reloadTheme() {
        reload(false) // todo see if there's a better solution
    }

    override fun onScrollTo() {
        //todo idk
    }

    override fun onScrollFrom() {
        //todo idk
    }

    /*
     * ---------------------------------------------
     * Nested Scrolling Content
     * ---------------------------------------------
     */

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