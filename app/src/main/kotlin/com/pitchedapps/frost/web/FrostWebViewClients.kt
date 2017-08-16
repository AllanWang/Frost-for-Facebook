package com.pitchedapps.frost.web

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pitchedapps.frost.activities.LoginActivity
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.SelectorActivity
import com.pitchedapps.frost.activities.WebOverlayActivity
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.injectors.*
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.utils.iab.IS_FROST_PRO
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
open class FrostWebViewClient(val webCore: FrostWebViewCore) : BaseWebViewClient() {

    val refreshObservable: Subject<Boolean> = webCore.refreshObservable
    val isMain = webCore.baseEnum != null

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url == null) return
        L.i("FWV Loading", url)
        refreshObservable.onNext(true)
        if (!url.isFacebookUrl) return
        if (url.contains("logout.php")) FbCookie.logout(Prefs.userId, { launchLogin(view.context) })
        else if (url.contains("login.php")) FbCookie.reset({ launchLogin(view.context) })
    }


    fun launchLogin(c: Context) {
        if (c is MainActivity && c.cookies().isNotEmpty())
            c.launchNewTask(SelectorActivity::class.java, c.cookies())
        else
            c.launchNewTask(LoginActivity::class.java)
    }

    fun injectBackgroundColor()
            = webCore.setBackgroundColor(if (isMain) Color.TRANSPARENT else Prefs.bgColor.withAlpha(255))


    override fun onPageCommitVisible(view: WebView, url: String?) {
        super.onPageCommitVisible(view, url)
        injectBackgroundColor()
        if (url.isFacebookUrl)
            view.jsInject(
                    CssAssets.ROUND_ICONS.maybe(Prefs.showRoundedIcons),
                    CssHider.HEADER,
                    CssHider.PEOPLE_YOU_MAY_KNOW.maybe(!Prefs.showSuggestedFriends && IS_FROST_PRO),
                    Prefs.themeInjector,
                    CssHider.NON_RECENT.maybe(webCore.url?.contains("?sk=h_chr") ?: false))
    }

    override fun onPageFinished(view: WebView, url: String?) {
        url ?: return
        L.i("Page finished", url)
        if (!url.isFacebookUrl) {
            refreshObservable.onNext(false)
            return
        }
        onPageFinishedActions(url)
    }

    open internal fun onPageFinishedActions(url: String) {
        if (url.startsWith("${FbItem.MESSAGES.url}/read/") && Prefs.messageScrollToBottom)
            webCore.pageDown(true)
        injectAndFinish()
    }

    internal fun injectAndFinish() {
        L.d("Page finished reveal")
        refreshObservable.onNext(false)
        injectBackgroundColor()
        webCore.jsInject(
                JsActions.LOGIN_CHECK,
                JsAssets.CLICK_A.maybe(webCore.baseEnum != null && Prefs.overlayEnabled),
                JsAssets.TEXTAREA_LISTENER,
                CssHider.ADS.maybe(!Prefs.showFacebookAds && IS_FROST_PRO),
                JsAssets.CONTEXT_A,
                JsAssets.HEADER_BADGES.maybe(webCore.baseEnum != null)
        )
    }

    open fun handleHtml(html: String?) {
        L.d("Handle Html")
    }

    open fun emit(flag: Int) {
        L.d("Emit $flag")
    }

    /**
     * Helper to format the request and launch it
     * returns true to override the url
     * returns false if we are already in an overlaying activity
     */
    private fun launchRequest(request: WebResourceRequest): Boolean {
        L.d("Launching Url", request.url?.toString() ?: "null")
        return webCore.context !is WebOverlayActivity && webCore.context.requestWebOverlay(request.url.toString())
    }

    private fun launchImage(url: String, text: String? = null): Boolean {
        L.d("Launching Image", url)
        webCore.context.launchImageActivity(url, text)
        if (webCore.canGoBack()) webCore.goBack()
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        L.i("Url Loading", request.url?.toString())
        val path = request.url?.path ?: return super.shouldOverrideUrlLoading(view, request)
        L.v("Url Loading Path", path)
        if (path.startsWith("/composer/")) return launchRequest(request)
        if (request.url.toString().contains("scontent-sea1-1.xx.fbcdn.net") && (path.endsWith(".jpg") || path.endsWith(".png")))
            return launchImage(request.url.toString())
        if (view.context.resolveActivityForUri(request.url)) return true
        return super.shouldOverrideUrlLoading(view, request)
    }

}

/**
 * Client variant for the menu view
 */
class FrostWebViewClientMenu(webCore: FrostWebViewCore) : FrostWebViewClient(webCore) {

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
        super.injectAndFinish()
    }

    override fun onPageFinishedActions(url: String) {
        L.d("Should inject ${url.shouldInjectMenu}")
        if (!url.shouldInjectMenu) injectAndFinish()
    }
}

/**
 * Headless client that injects content after a page load
 * The JSI is meant to handle everything else
 */
class HeadlessWebViewClient(val tag: String, val postInjection: InjectorContract) : BaseWebViewClient() {

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url == null) return
        L.d("Headless Page $tag Started", url)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        if (url == null) return
        L.d("Headless Page $tag Finished", url)
        postInjection.inject(view)
    }

    /**
     * In addition to general filtration, we will also strip away css and images
     */
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse?
            = super.shouldInterceptRequest(view, request).filterCss(request).filterImage(request)

}