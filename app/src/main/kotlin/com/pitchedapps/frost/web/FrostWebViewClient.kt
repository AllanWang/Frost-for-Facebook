package com.pitchedapps.frost.web

import android.content.Context
import android.graphics.Bitmap
import android.view.KeyEvent
import android.webkit.*
import ca.allanwang.kau.utils.isVisible
import com.pitchedapps.frost.LoginActivity
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.SelectorActivity
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.views.circularReveal
import com.pitchedapps.frost.views.fadeIn
import com.pitchedapps.frost.views.fadeOut
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-31.
 */
open class FrostWebViewClient(val refreshObservable: Subject<Boolean>) : WebViewClient() {

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        L.i("FWV Loading $url")
        L.v("Cookies ${CookieManager.getInstance().getCookie(url)}")
        refreshObservable.onNext(true)
        if (!url.contains(FACEBOOK_COM)) return
        if (url.contains("logout.php")) FbCookie.logout(Prefs.userId, { launchLogin(view.context) })
        else if (url.contains("login.php")) FbCookie.reset({ launchLogin(view.context) })
        view.fadeOut(duration = 200L)
    }

    fun launchLogin(c: Context) {
        if (c is MainActivity && c.cookies().isNotEmpty())
            c.launchNewTask(SelectorActivity::class.java, c.cookies())
        else
            c.launchNewTask(LoginActivity::class.java)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (!url.contains(FACEBOOK_COM)) {
            refreshObservable.onNext(false)
            if (!view.isVisible()) view.fadeIn(duration = 200L)
            return
        }
        L.i("Page finished $url")
        JsActions.LOGIN_CHECK.inject(view)
        onPageFinishedReveal(view as FrostWebViewCore, url)
    }

    open internal fun onPageFinishedReveal(view: FrostWebViewCore, url: String?) {
        onPageFinishedReveal(view, true)
    }

    internal fun onPageFinishedReveal(view: FrostWebViewCore, animate: Boolean) {
        L.d("Page finished reveal")
        CssAssets.HEADER.inject(view, {
            refreshObservable.onNext(false)
            if (animate) view.circularReveal(offset = 150L)
            else view.fadeIn(duration = 100L)
        })
    }

    open fun handleHtml(html: String) {
        L.d("Handle Html")
    }

    open fun emit(flag: Int) {
        L.d("Emit $flag")
    }

    fun inject(jsAssets: JsAssets, view: WebView, callback: (String) -> Unit = {}) {
        L.i("Post inject ${jsAssets.name}")
        jsAssets.inject(view, {
            L.i("Post injection done $it")
            callback.invoke(it)
        })
    }

    override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean {
        L.d("Key event ${event.keyCode}")
        return super.shouldOverrideKeyEvent(view, event)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
        L.d("Url Loading ${request?.url?.path}")
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest?): WebResourceResponse? {
        if (request == null || !(request.url.host?.contains(FACEBOOK_COM) ?: false)) return super.shouldInterceptRequest(view, request)
        L.v("Url intercept ${request.url.path}")
        return super.shouldInterceptRequest(view, request)
    }

    override fun onLoadResource(view: WebView, url: String) {
        if (!url.contains(FACEBOOK_COM)) return super.onLoadResource(view, url)
        L.v("Resource $url")
//        FrostWebOverlay.values.forEach {
//            if (url.contains(it.match))
//                L.d("Resource Loaded $it")
//        }
        super.onLoadResource(view, url)
    }

}