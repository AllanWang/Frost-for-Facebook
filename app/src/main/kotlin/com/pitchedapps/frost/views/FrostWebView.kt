package com.pitchedapps.frost.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import ca.allanwang.kau.utils.circularReveal
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.isVisible
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostDownload
import com.pitchedapps.frost.web.*
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by Allan Wang on 2017-05-29.
 *
 */
class FrostWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedWebView(context, attrs, defStyleAttr),
        FrostContentCore {

    override fun reload(animate: Boolean) {
        registerTransition(animate)
        super.reload()
    }

    override lateinit var parent: FrostContentParent

    internal lateinit var frostWebClient: FrostWebViewClient

    override val currentUrl: String
        get() = url ?: ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun bind(container: FrostContentContainer): View {
        with(settings) {
            javaScriptEnabled = true
            if (parent.baseUrl.shouldUseBasicAgent)
                userAgentString = USER_AGENT_BASIC
            allowFileAccess = true
            textZoom = Prefs.webTextScaling
        }
        setLayerType(LAYER_TYPE_HARDWARE, null)
        // attempt to get custom client; otherwise fallback to original
        frostWebClient = (container as? WebFragment)?.client(this) ?: FrostWebViewClient(this)
        webViewClient = frostWebClient
        webChromeClient = FrostChromeClient(this)
        addJavascriptInterface(FrostJSI(this), "Frost")
        setBackgroundColor(Color.TRANSPARENT)
        setDownloadListener(context::frostDownload)
        return this
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
    private fun registerTransition(animate: Boolean) {
        var dispose: Disposable? = null
        var loading = false
        dispose = parent.refreshObservable.subscribeOn(AndroidSchedulers.mainThread()).subscribe {
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
        loadUrl(parent.baseUrl, animate)
    }

    fun addTitleListener(subscriber: (title: String) -> Unit, scheduler: Scheduler = AndroidSchedulers.mainThread()): Disposable
            = parent.titleObservable.observeOn(scheduler).subscribe(subscriber)

    override fun onBackPressed(): Boolean {
        if (canGoBack()) {
            goBack()
            return true
        }
        return false
    }

    /**
     * If webview is already at the top, refresh
     * Otherwise scroll to top
     */
    override fun onTabClicked() {
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

    override var active: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            // todo
        }

    override fun reloadTheme() {
        reloadThemeSelf()
    }

    override fun reloadThemeSelf() {
        reload(false) // todo see if there's a better solution
    }

    override fun reloadTextSize() {
        reloadTextSizeSelf()
    }

    override fun reloadTextSizeSelf() {
        settings.textZoom = Prefs.webTextScaling
    }

}