package com.pitchedapps.frost.web

import android.content.Context
import android.graphics.Bitmap
import android.view.KeyEvent
import android.webkit.*
import com.pitchedapps.frost.LoginActivity
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.SelectorActivity
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.views.circularReveal
import com.pitchedapps.frost.views.fadeOut
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostWebViewClient(val refreshObservable: Subject<Boolean>) : WebViewClient() {

    companion object {
        //Collections of jewels mapped with url match -> id
        val jewelMap: Map<String, String> = mapOf("a" to "b")

        fun test() {

        }
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        L.i("FWV Loading $url")
        L.i("Cookies ${CookieManager.getInstance().getCookie(url)}")
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
            c.launchNewTask(LoginActivity::class.java, clearStack = false)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        refreshObservable.onNext(false)
        if (!url.contains(FACEBOOK_COM)) return
        JsActions.LOGIN_CHECK.inject(view)
        CssAssets.HEADER.inject(view, {
            view.circularReveal(offset = 150L)
        })
    }

    override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean {
        L.d("Key event ${event.keyCode}")
        return super.shouldOverrideKeyEvent(view, event)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        L.d("Url Loading ${request.url?.path}")
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (!request.url.host.contains(FACEBOOK_COM)) return super.shouldInterceptRequest(view, request)
        L.v("Url intercept ${request.url.path}")
        return super.shouldInterceptRequest(view, request)
    }

    override fun onLoadResource(view: WebView, url: String) {
        if (!url.contains(FACEBOOK_COM)) return super.onLoadResource(view, url)
        L.v("Resource $url")
        FrostWebOverlay.values.forEach {
            if (url.contains(it.match))
                L.d("Resource Loaded $it")
        }
        super.onLoadResource(view, url)
    }

    fun logout() {

    }

}