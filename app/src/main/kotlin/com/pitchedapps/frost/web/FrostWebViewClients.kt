package com.pitchedapps.frost.web

import android.graphics.Bitmap
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.injectors.*
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.utils.iab.IS_FROST_PRO
import com.pitchedapps.frost.views.FrostWebView
import io.reactivex.subjects.Subject
import org.jetbrains.anko.withAlpha

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

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse?
            = view.shouldFrostInterceptRequest(request)

}

/**
 * The default webview client
 */
open class FrostWebViewClient(val web: FrostWebView) : BaseWebViewClient() {

    private val refresh: Subject<Boolean> = web.parent.refreshObservable
    private val isMain = web.parent.baseEnum != null

    protected inline fun v(crossinline message: () -> Any?) = L.v { "web client: ${message()}" }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url == null) return
        v { "loading $url" }
        refresh.onNext(true)
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
        if (url.isFacebookUrl)
            view.jsInject(
                    CssAssets.ROUND_ICONS.maybe(Prefs.showRoundedIcons),
                    CssHider.HEADER,
                    CssHider.CORE,
                    CssHider.COMPOSER.maybe(!Prefs.showComposer),
                    CssHider.PEOPLE_YOU_MAY_KNOW.maybe(!Prefs.showSuggestedFriends && IS_FROST_PRO),
                    CssHider.SUGGESTED_GROUPS.maybe(!Prefs.showSuggestedGroups && IS_FROST_PRO),
                    Prefs.themeInjector,
                    CssHider.NON_RECENT.maybe((web.url?.contains("?sk=h_chr") ?: false)
                            && Prefs.aggressiveRecents),
                    JsAssets.DOCUMENT_WATCHER)
        else
            refresh.onNext(false)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        url ?: return
        v { "finished $url" }
        if (!url.isFacebookUrl) {
            refresh.onNext(false)
            return
        }
        onPageFinishedActions(url)
    }

    open internal fun onPageFinishedActions(url: String) {
        if (url.startsWith("${FbItem.MESSAGES.url}/read/") && Prefs.messageScrollToBottom)
            web.pageDown(true)
        injectAndFinish()
    }

    internal fun injectAndFinish() {
        v { "page finished reveal" }
        refresh.onNext(false)
        injectBackgroundColor()
        web.jsInject(
                JsActions.LOGIN_CHECK,
                JsAssets.CLICK_A,
                JsAssets.TEXTAREA_LISTENER,
                CssHider.ADS.maybe(!Prefs.showFacebookAds && IS_FROST_PRO),
                JsAssets.CONTEXT_A,
                JsAssets.MEDIA,
                JsAssets.HEADER_BADGES.maybe(web.parent.baseEnum != null)
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

    private fun launchImage(url: String, text: String? = null): Boolean {
        v { "Launching image: $url" }
        web.context.launchImageActivity(url, text)
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
        if (path.startsWith("/composer/")) return launchRequest(request)
        if (url.isImageUrl)
            return launchImage(url)
        if (Prefs.linksInDefaultApp && view.context.resolveActivityForUri(request.url)) return true
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

    private val String.shouldInjectMenu
        get() = when (removePrefix(FB_URL_BASE)) {
            "settings",
            "settings#",
            "settings#!/settings?soft=bookmarks" -> true
            else -> false
        }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        if (url == null) return
        if (url.shouldInjectMenu) jsInject(JsAssets.MENU)
    }

    override fun emit(flag: Int) {
        super.emit(flag)
        when (flag) {
            EMIT_FINISH -> super.injectAndFinish()
        }
    }

    override fun onPageFinishedActions(url: String) {
        v { "Should inject ${url.shouldInjectMenu}" }
        if (!url.shouldInjectMenu) injectAndFinish()
    }
}