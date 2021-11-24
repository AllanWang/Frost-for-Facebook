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
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.AnimHolder
import ca.allanwang.kau.utils.ctxCoroutine
import ca.allanwang.kau.utils.launchMain
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.contracts.WebFileChooser
import com.pitchedapps.frost.db.CookieDao
import com.pitchedapps.frost.db.currentCookie
import com.pitchedapps.frost.facebook.FB_HOME_URL
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.USER_AGENT
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostDownload
import com.pitchedapps.frost.web.FrostChromeClient
import com.pitchedapps.frost.web.FrostWebClientEntryPoint
import com.pitchedapps.frost.web.FrostWebComponentBuilder
import com.pitchedapps.frost.web.FrostWebEntryPoint
import com.pitchedapps.frost.web.FrostWebViewClient
import com.pitchedapps.frost.web.FrostWebViewClientMenu
import com.pitchedapps.frost.web.FrostWebViewClientMessenger
import com.pitchedapps.frost.web.NestedWebView
import dagger.hilt.EntryPoints
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Allan Wang on 2017-05-29.
 *
 */
@AndroidEntryPoint
class FrostWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedWebView(context, attrs, defStyleAttr), FrostContentCore {

    @Inject
    lateinit var activity: Activity

    @Inject
    lateinit var fbCookie: FbCookie

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var themeProvider: ThemeProvider

    @Inject
    lateinit var webFileChooser: WebFileChooser

    @Inject
    lateinit var cookieDao: CookieDao

    @Inject
    lateinit var frostWebComponentBuilder: FrostWebComponentBuilder

    override fun reload(animate: Boolean) {
        if (parent.registerTransition(false, animate))
            super.reload()
    }

    override lateinit var parent: FrostContentParent

    internal lateinit var frostWebClient: FrostWebViewClient

    override val currentUrl: String
        get() = url ?: ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun bind(parent: FrostContentParent, container: FrostContentContainer): View {
        this.parent = parent
        val component = frostWebComponentBuilder.frostParent(parent).frostWebView(this).build()
        val webEntryPoint = EntryPoints.get(component, FrostWebEntryPoint::class.java)
        val clientEntryPoint = EntryPoints.get(component, FrostWebClientEntryPoint::class.java)
        userAgentString = USER_AGENT
        with(settings) {
            javaScriptEnabled = true
            mediaPlaybackRequiresUserGesture = false // TODO check if we need this
            allowFileAccess = true
            textZoom = prefs.webTextScaling
            domStorageEnabled = true
        }
        setLayerType(LAYER_TYPE_HARDWARE, null)
        // attempt to get custom client; otherwise fallback to original
        frostWebClient = when (parent.baseEnum) {
            FbItem.MESSENGER -> FrostWebViewClientMessenger(this)
            FbItem.MENU -> FrostWebViewClientMenu(this)
            else -> clientEntryPoint.webClient()
        }
        webViewClient = frostWebClient
        webChromeClient = FrostChromeClient(this, themeProvider, webFileChooser)
        addJavascriptInterface(webEntryPoint.frostJsi(), "Frost")
        setBackgroundColor(Color.TRANSPARENT)
        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            context.ctxCoroutine.launchMain {
                val cookie = cookieDao.currentCookie(prefs) ?: return@launchMain
                context.frostDownload(
                    cookie.cookie,
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
     * 2018-10-17. facebook automatically adds their home page to the back stack,
     * regardless of the loaded url. We will make sure we skip it when going back.
     *
     * 2019-10-14. Looks like facebook now randomly populates some links with the home page,
     * especially those that are launched with a blank target...
     * In some cases, there can be more than one home target in a row.
     */
    override fun onBackPressed(): Boolean {
        val list = copyBackForwardList()
        if (list.currentIndex >= 2) {
            val skipCount = (1..list.currentIndex).firstOrNull {
                list.getItemAtIndex(list.currentIndex - it).url != FB_HOME_URL
            } ?: return false // If no non home url is found, we will treat the stack as empty
            L.v { "onBackPress: going back ${if (skipCount == 1) "one page" else "$skipCount pages"}" }
            goBackOrForward(-skipCount)
            return true
        }
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
        settings.textZoom = prefs.webTextScaling
    }

    override fun destroy() {
        (getParent() as? ViewGroup)?.removeView(this)
        super.destroy()
    }
}
