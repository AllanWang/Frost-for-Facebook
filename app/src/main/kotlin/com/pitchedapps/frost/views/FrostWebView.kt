package com.pitchedapps.frost.views

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.AnimHolder
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostDownload
import com.pitchedapps.frost.web.*

/**
 * Created by Allan Wang on 2017-05-29.
 *
 */
class FrostWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedWebView(context, attrs, defStyleAttr),
        FrostContentCore {

    override fun reload(animate: Boolean) {
        if (parent.registerTransition(false, animate))
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
        if (parent.registerTransition(this.url != url, animate))
            super.loadUrl(url)
    }

    override fun reloadBase(animate: Boolean) {
        loadUrl(parent.baseUrl, animate)
    }

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
        if (scrollY > 10000)
            scrollTo(0, 0)
        else
            smoothScrollTo(0)
    }

    private fun smoothScrollTo(y: Int) {
        ValueAnimator.ofInt(scrollY, y).apply {
            duration = Math.min(Math.abs(scrollY - y), 500).toLong()
            interpolator = AnimHolder.fastOutSlowInInterpolator(context)
            addUpdateListener { scrollY = it.animatedValue as Int }
            start()
        }
    }

    private fun smoothScrollBy(y: Int) = smoothScrollTo(Math.max(0, scrollY + y))

    override var active: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            if (field) onResume()
            else onPause()
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

    override fun destroy() {
        val parent = getParent() as? ViewGroup
        if (parent != null) {
            parent.removeView(this)
            super.destroy()
        }
    }
}