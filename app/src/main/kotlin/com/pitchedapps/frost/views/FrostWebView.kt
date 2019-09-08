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

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.AnimHolder
import ca.allanwang.kau.utils.launchMain
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.db.FrostDatabase
import com.pitchedapps.frost.db.currentCookie
import com.pitchedapps.frost.facebook.FB_HOME_URL
import com.pitchedapps.frost.facebook.USER_AGENT
import com.pitchedapps.frost.fragments.WebFragment
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.ctxCoroutine
import com.pitchedapps.frost.utils.frostDownload
import com.pitchedapps.frost.web.FrostChromeClient
import com.pitchedapps.frost.web.FrostJSI
import com.pitchedapps.frost.web.FrostWebViewClient
import com.pitchedapps.frost.web.NestedWebView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Allan Wang on 2017-05-29.
 *
 */
class FrostWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
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
        userAgentString = USER_AGENT
        with(settings) {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false // TODO check if we need this
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
        val db = FrostDatabase.get()
        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            context.ctxCoroutine.launchMain {
                val cookie = db.cookieDao().currentCookie() ?: return@launchMain
                context.frostDownload(
                    cookie,
                    url,
                    userAgent,
                    contentDisposition,
                    mimetype,
                    contentLength
                )
            }
        }
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

    /**
     * By 2018-10-17, facebook automatically adds their home page to the back stack,
     * regardless of the loaded url. We will make sure we skip it when going back.
     */
    override fun onBackPressed(): Boolean {
        if (canGoBackOrForward(-2)) {
            goBack()
            return true
        }
        val list = copyBackForwardList()
        if (list.currentIndex == 1 && list.getItemAtIndex(0).url == FB_HOME_URL) {
            return false
        }
        if (list.currentIndex > 0) {
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
            duration = min(abs(scrollY - y), 500).toLong()
            interpolator = AnimHolder.fastOutSlowInInterpolator(context)
            addUpdateListener { scrollY = it.animatedValue as Int }
            start()
        }
    }

    private fun smoothScrollBy(y: Int) = smoothScrollTo(max(0, scrollY + y))

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
