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
package com.pitchedapps.frost.web

import android.graphics.Bitmap
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import ca.allanwang.kau.utils.ctxCoroutine
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.db.CookieDao
import com.pitchedapps.frost.db.currentCookie
import com.pitchedapps.frost.db.updateMessengerCookie
import com.pitchedapps.frost.enums.ThemeCategory
import com.pitchedapps.frost.facebook.FACEBOOK_BASE_COM
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.HTTPS_MESSENGER_COM
import com.pitchedapps.frost.facebook.MESSENGER_THREAD_PREFIX
import com.pitchedapps.frost.facebook.WWW_FACEBOOK_COM
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.injectors.CssAsset
import com.pitchedapps.frost.injectors.CssHider
import com.pitchedapps.frost.injectors.InjectorContract
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.isExplicitIntent
import com.pitchedapps.frost.utils.isFacebookUrl
import com.pitchedapps.frost.utils.isFbCookie
import com.pitchedapps.frost.utils.isImageUrl
import com.pitchedapps.frost.utils.isIndirectImageUrl
import com.pitchedapps.frost.utils.isMessengerUrl
import com.pitchedapps.frost.utils.launchImageActivity
import com.pitchedapps.frost.utils.startActivityForUri
import com.pitchedapps.frost.views.FrostWebView
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Collection of webview clients
 */

/**
 * The base of all webview clients
 * Used to ensure that resources are properly intercepted
 */
open class BaseWebViewClient : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? =
        view.shouldFrostInterceptRequest(request)
}

/**
 * The default webview client
 */
open class FrostWebViewClient(val web: FrostWebView) : BaseWebViewClient() {

    protected val fbCookie: FbCookie get() = web.fbCookie
    protected val prefs: Prefs get() = web.prefs
    protected val themeProvider: ThemeProvider get() = web.themeProvider
    protected val refresh: SendChannel<Boolean> = web.parent.refreshChannel
    protected val isMain = web.parent.baseEnum != null

    /**
     * True if current url supports refresh. See [doUpdateVisitedHistory] for updates
     */
    internal var urlSupportsRefresh: Boolean = true

    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        urlSupportsRefresh = urlSupportsRefresh(url)
        web.parent.swipeAllowedByPage = urlSupportsRefresh
        view.jsInject(
            JsAssets.AUTO_RESIZE_TEXTAREA.maybe(prefs.autoExpandTextBox),
            prefs = prefs
        )
        v { "History $url; refresh $urlSupportsRefresh" }
    }

    private fun urlSupportsRefresh(url: String?): Boolean {
        if (url == null) return false
        if (url.isMessengerUrl) return false
        if (!url.isFacebookUrl) return true
        if (url.contains("soft=composer")) return false
        if (url.contains("sharer.php") || url.contains("sharer-dialog.php")) return false
        return true
    }

    protected inline fun v(crossinline message: () -> Any?) = L.v { "web client: ${message()}" }

    /**
     * Main injections for facebook content
     */
    protected open val facebookJsInjectors: List<InjectorContract> = listOf(
        //                    CssHider.CORE,
        CssHider.HEADER,
        CssHider.COMPOSER.maybe(!prefs.showComposer),
        CssHider.STORIES.maybe(!prefs.showStories),
        CssHider.PEOPLE_YOU_MAY_KNOW.maybe(!prefs.showSuggestedFriends),
        CssHider.SUGGESTED_GROUPS.maybe(!prefs.showSuggestedGroups),
        themeProvider.injector(ThemeCategory.FACEBOOK),
        CssHider.NON_RECENT.maybe(
            (web.url?.contains("?sk=h_chr") ?: false) &&
                prefs.aggressiveRecents
        ),
        CssHider.ADS.maybe(!prefs.showFacebookAds),
        CssHider.POST_ACTIONS.maybe(!prefs.showPostActions),
        CssHider.POST_REACTIONS.maybe(!prefs.showPostReactions),
        CssAsset.FullSizeImage.maybe(prefs.fullSizeImage),
        JsAssets.DOCUMENT_WATCHER,
        JsAssets.HORIZONTAL_SCROLLING,
        JsAssets.AUTO_RESIZE_TEXTAREA.maybe(prefs.autoExpandTextBox),
//            JsAssets.CLICK_A,
        JsAssets.CONTEXT_A,
        JsAssets.MEDIA,
        JsAssets.SCROLL_STOP,
    )

    private fun WebView.facebookJsInject() {
        jsInject(*facebookJsInjectors.toTypedArray(), prefs = prefs)
    }

    private fun WebView.messengerJsInject() {
        jsInject(
            themeProvider.injector(ThemeCategory.MESSENGER),
            prefs = prefs
        )
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url == null) return
        v { "loading $url ${web.settings.userAgentString}" }
        refresh.offer(true)
    }

    private fun injectBackgroundColor() {
        web.setBackgroundColor(
            when {
                isMain -> Color.TRANSPARENT
                web.url.isFacebookUrl -> themeProvider.bgColor.withAlpha(255)
                else -> Color.WHITE
            }
        )
    }

    override fun onPageCommitVisible(view: WebView, url: String?) {
        super.onPageCommitVisible(view, url)
        injectBackgroundColor()
        when {
            url.isFacebookUrl -> {
                v { "FB Page commit visible" }
                view.facebookJsInject()
            }
            url.isMessengerUrl -> {
                v { "Messenger Page commit visible" }
                view.messengerJsInject()
            }
            else -> {
                refresh.offer(false)
            }
        }
    }

    override fun onPageFinished(view: WebView, url: String?) {
        url ?: return
        v { "finished $url" }
        if (!url.isFacebookUrl && !url.isMessengerUrl) {
            refresh.offer(false)
            return
        }
        onPageFinishedActions(url)
    }

    internal open fun onPageFinishedActions(url: String) {
        if (url.startsWith("${FbItem.MESSAGES.url}/read/") && prefs.messageScrollToBottom) {
            web.pageDown(true)
        }
        injectAndFinish()
    }

    internal fun injectAndFinish() {
        v { "page finished reveal" }
        refresh.offer(false)
        injectBackgroundColor()
        when {
            web.url.isFacebookUrl -> {
                web.jsInject(
                    JsActions.LOGIN_CHECK,
                    JsAssets.TEXTAREA_LISTENER,
                    JsAssets.HEADER_BADGES.maybe(isMain),
                    prefs = prefs
                )
                web.facebookJsInject()
            }
            web.url.isMessengerUrl -> {
                web.messengerJsInject()
            }
        }
    }

    open fun handleHtml(html: String?) {
        L.d { "Handle Html" }
    }

    open fun emit(flag: Int) {
        L.d { "Emit $flag" }
    }

    /**
     * Helper to format the request and launch it
     * returns true to override the url
     * returns false if we are already in an overlaying activity
     */
    private fun launchRequest(request: WebResourceRequest): Boolean {
        v { "Launching url: ${request.url}" }
        return web.requestWebOverlay(request.url.toString())
    }

    private fun launchImage(url: String, text: String? = null, cookie: String? = null): Boolean {
        v { "Launching image: $url" }
        web.context.launchImageActivity(url, text, cookie)
        if (web.canGoBack()) web.goBack()
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        v { "Url loading: ${request.url}" }
        val path = request.url?.path ?: return super.shouldOverrideUrlLoading(view, request)
        v { "Url path $path" }
        val url = request.url.toString()
        if (url.isExplicitIntent) {
            view.context.startActivityForUri(request.url)
            return true
        }
        if (path.startsWith("/composer/")) {
            return launchRequest(request)
        }
        if (url.isIndirectImageUrl) {
            return launchImage(url.formattedFbUrl, cookie = fbCookie.webCookie)
        }
        if (url.isImageUrl) {
            return launchImage(url.formattedFbUrl)
        }
        if (prefs.linksInDefaultApp && view.context.startActivityForUri(request.url)) {
            return true
        }
        // Convert desktop urls to mobile ones
        if (url.contains("https://www.facebook.com") && urlSupportsRefresh(url)) {
            view.loadUrl(url.replace(WWW_FACEBOOK_COM, FACEBOOK_BASE_COM))
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
}

private const val EMIT_THEME = 0b1
private const val EMIT_ID = 0b10
private const val EMIT_COMPLETE = EMIT_THEME or EMIT_ID
private const val EMIT_FINISH = 0

/**
 * Client variant for the menu view
 */
class FrostWebViewClientMenu(web: FrostWebView) : FrostWebViewClient(web) {

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        if (url == null) {
            return
        }
        jsInject(JsAssets.MENU, prefs = prefs)
    }

    /*
     * We do not inject headers as they include the menu flyout.
     * Instead, we remove the flyout margins within the js script so that it covers the header.
     */
    override val facebookJsInjectors: List<InjectorContract> =
        super.facebookJsInjectors - CssHider.HEADER + CssAsset.Menu

    override fun emit(flag: Int) {
        super.emit(flag)
        when (flag) {
            EMIT_FINISH -> {
                super.injectAndFinish()
            }
        }
    }

    /*
     * Facebook doesn't properly load back to the menu even in standard browsers.
     * Instead, if we detect the base soft url, we will manually click the menu item
     */
    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        if (url?.startsWith(FbItem.MENU.url) == true) {
            jsInject(JsAssets.MENU_QUICK, prefs = prefs)
        }
    }

    override fun onPageFinishedActions(url: String) {
        // Skip
    }
}

class FrostWebViewClientMessenger(web: FrostWebView) : FrostWebViewClient(web) {

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        messengerCookieCheck(url!!)
    }

    private val cookieDao: CookieDao get() = web.cookieDao
    private var hasCookie = fbCookie.messengerCookie.isFbCookie

    /**
     * Check cookie changes. Unlike fb checks, we will continuously poll for cookie changes during loading.
     * There is no lifecycle association between messenger login and facebook login,
     * so we'll try to be smart about when to check for state changes.
     *
     * From testing, it looks like this is called after redirects.
     * We can therefore classify no login as pointing to messenger.com,
     * and login as pointing to messenger.com/t/[thread id]
     */
    private fun messengerCookieCheck(url: String?) {
        if (url?.startsWith(HTTPS_MESSENGER_COM) != true) return
        val shouldHaveCookie = url.startsWith(MESSENGER_THREAD_PREFIX)
        L._d { "Messenger client: $url $shouldHaveCookie" }
        if (shouldHaveCookie == hasCookie) return
        hasCookie = shouldHaveCookie
        web.context.ctxCoroutine.launch {
            cookieDao.updateMessengerCookie(
                prefs.userId,
                if (shouldHaveCookie) fbCookie.messengerCookie else null
            )
            L._d { "New cookie ${cookieDao.currentCookie(prefs)?.toSensitiveString()}" }
        }
    }
}
