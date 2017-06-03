package com.pitchedapps.frost.web

import android.graphics.Bitmap
import android.view.KeyEvent
import android.webkit.*
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.views.circularReveal
import com.pitchedapps.frost.views.fadeOut

/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostWebViewClient(val position: () -> Int) : WebViewClient() {

    companion object {
        //Collections of jewels mapped with url match -> id
        val jewelMap: Map<String, String> = mapOf("a" to "b")

        fun test() {

        }
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        L.i("FWV Loading $url")
        if (!url.contains(FACEBOOK_COM)) return
        if (url.contains("logout.php")) FbCookie.logout(position.invoke())
        view.fadeOut(duration = 200L)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (!url.contains(FACEBOOK_COM)) return
        FbCookie.checkUserId(url, CookieManager.getInstance().getCookie(url), position.invoke())
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