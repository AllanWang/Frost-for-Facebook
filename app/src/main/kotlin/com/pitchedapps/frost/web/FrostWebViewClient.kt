package com.pitchedapps.frost.web

import android.graphics.Bitmap
import android.view.View
import android.webkit.*
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.utils.L
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostWebViewClient(val observable: Subject<WebStatus>) : WebViewClient() {

    companion object {
        //Collections of jewels mapped with url match -> id
        val jewelMap: Map<String, String> = mapOf("a" to "b")

        fun test() {

        }
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        observable.onNext(WebStatus.ERROR)
        L.e("FWV Error ${request}")
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        observable.onNext(WebStatus.LOADING)
        L.d("FWV Loading $url")
        if (!url.contains(FACEBOOK_COM)) return
        if (url.contains("logout.php")) FbCookie.logout()
        view.visibility = View.INVISIBLE
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (!url.contains(FACEBOOK_COM)) return
        observable.onNext(WebStatus.LOADED)
        FbCookie.checkUserId(url, CookieManager.getInstance().getCookie(url))
        CssAssets.BASE.inject(view, {
            view.visibility = View.VISIBLE
        })
    }

    fun logout() {

    }

}