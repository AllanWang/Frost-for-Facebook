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
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.injectors.CssHider
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.isExplicitIntent
import com.pitchedapps.frost.utils.isFacebookUrl
import com.pitchedapps.frost.utils.isImageUrl
import com.pitchedapps.frost.utils.isIndirectImageUrl
import com.pitchedapps.frost.utils.launchImageActivity
import com.pitchedapps.frost.utils.resolveActivityForUri
import com.pitchedapps.frost.views.FrostWebView
import kotlinx.coroutines.channels.SendChannel

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

    private val refresh: SendChannel<Boolean> = web.parent.refreshChannel
    private val isMain = web.parent.baseEnum != null

    protected inline fun v(crossinline message: () -> Any?) = L.v { "web client: ${message()}" }

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
                web.url.isFacebookUrl -> Prefs.bgColor.withAlpha(255)
                else -> Color.WHITE
            }
        )
    }

    override fun onPageCommitVisible(view: WebView, url: String?) {
        super.onPageCommitVisible(view, url)
        injectBackgroundColor()
        if (url.isFacebookUrl) {
            view.jsInject(
//                    CssHider.CORE,
                CssHider.HEADER,
                CssHider.COMPOSER.maybe(!Prefs.showComposer),
                CssHider.STORIES.maybe(!Prefs.showStories),
                CssHider.PEOPLE_YOU_MAY_KNOW.maybe(!Prefs.showSuggestedFriends),
                CssHider.SUGGESTED_GROUPS.maybe(!Prefs.showSuggestedGroups),
                Prefs.themeInjector,
                CssHider.NON_RECENT.maybe(
                    (web.url?.contains("?sk=h_chr") ?: false) &&
                        Prefs.aggressiveRecents
                ),
                JsAssets.DOCUMENT_WATCHER,
                JsAssets.HORIZONTAL_SCROLLING,
                JsAssets.CLICK_A,
                CssHider.ADS.maybe(!Prefs.showFacebookAds),
                JsAssets.CONTEXT_A,
                JsAssets.MEDIA
            )
        } else {
            refresh.offer(false)
        }
    }

    override fun onPageFinished(view: WebView, url: String?) {
        url ?: return
        v { "finished $url" }
        if (!url.isFacebookUrl) {
            refresh.offer(false)
            return
        }
        onPageFinishedActions(url)
    }

    internal open fun onPageFinishedActions(url: String) {
        if (url.startsWith("${FbItem.MESSAGES.url}/read/") && Prefs.messageScrollToBottom)
            web.pageDown(true)
        injectAndFinish()
    }

    internal fun injectAndFinish() {
        v { "page finished reveal" }
        refresh.offer(false)
        injectBackgroundColor()
        web.jsInject(
            JsActions.LOGIN_CHECK,
            JsAssets.TEXTAREA_LISTENER,
            JsAssets.HEADER_BADGES.maybe(isMain)
        )
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
            view.context.resolveActivityForUri(request.url)
            return true
        }
        if (path.startsWith("/composer/")) {
            return launchRequest(request)
        }
        if (url.isIndirectImageUrl) {
            return launchImage(url.formattedFbUrl, cookie = FbCookie.webCookie)
        }
        if (url.isImageUrl) {
            return launchImage(url.formattedFbUrl)
        }
        if (Prefs.linksInDefaultApp && view.context.resolveActivityForUri(request.url)) {
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
        jsInject(JsAssets.MENU)
    }

    override fun emit(flag: Int) {
        super.emit(flag)
        when (flag) {
            EMIT_FINISH -> {
                super.injectAndFinish()
            }
        }
    }

    override fun onPageFinishedActions(url: String) {
        // Skip
    }
}
