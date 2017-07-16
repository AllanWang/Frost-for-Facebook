package com.pitchedapps.frost.web

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.pitchedapps.frost.activities.LoginActivity
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.SelectorActivity
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.*
import com.pitchedapps.frost.utils.*
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-31.
 */
open class FrostWebViewClient(val webCore: FrostWebViewCore) : BaseWebViewClient() {

    val refreshObservable: Subject<Boolean> = webCore.refreshObservable

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        L.i("FWV Loading $url")
//        L.v("Cookies ${CookieManager.getInstance().getCookie(url)}")
        refreshObservable.onNext(true)
        if (!url.contains(FACEBOOK_COM)) return
        if (url.contains("logout.php")) FbCookie.logout(Prefs.userId, { launchLogin(view.context) })
        else if (url.contains("login.php")) FbCookie.reset({ launchLogin(view.context) })
    }

    fun launchLogin(c: Context) {
        if (c is MainActivity && c.cookies().isNotEmpty())
            c.launchNewTask(SelectorActivity::class.java, c.cookies())
        else
            c.launchNewTask(LoginActivity::class.java)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        L.i("Page finished $url")
        if (!url.contains(FACEBOOK_COM)) {
            refreshObservable.onNext(false)
            return
        }
        view.jsInject(
                CssAssets.ROUND_ICONS.maybe(Prefs.showRoundedIcons),
                CssHider.PEOPLE_YOU_MAY_KNOW.maybe(!Prefs.showSuggestedFriends && Prefs.pro),
                CssHider.ADS.maybe(!Prefs.showFacebookAds && Prefs.pro)
        )
        onPageFinishedActions(url)
    }

    open internal fun onPageFinishedActions(url: String) {
        injectAndFinish()
    }

    internal fun injectAndFinish() {
        L.d("Page finished reveal")
        webCore.jsInject(CssHider.HEADER,
                Prefs.themeInjector,
                callback = {
                    refreshObservable.onNext(false)
                    webCore.jsInject(
                            JsActions.LOGIN_CHECK,
                            JsAssets.CLICK_A.maybe(webCore.baseEnum != null && Prefs.overlayEnabled),
                            JsAssets.CONTEXT_A,
                            JsAssets.HEADER_BADGES.maybe(webCore.baseEnum != null)
                    )
                })
    }

    open fun handleHtml(html: String) {
        L.d("Handle Html")
    }

    open fun emit(flag: Int) {
        L.d("Emit $flag")
    }

    /**
     * Helper to format the request and launch it
     * returns true to override the url
     */
    private fun launchRequest(request: WebResourceRequest): Boolean {
        L.d("Launching ${request.url}")
        webCore.context.launchWebOverlay(request.url.toString())
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        L.i("Url Loading ${request.url}")
        val path = request.url.path ?: return super.shouldOverrideUrlLoading(view, request)
        if (path.startsWith("/composer/")) return launchRequest(request)
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        L.d("ASDF PCV")
        super.onPageCommitVisible(view, url)
    }

//    override fun onLoadResource(view: WebView, url: String) {
//        L.v("Load resource $url")
//        super.onLoadResource(view, url)
//    }

}