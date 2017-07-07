package com.pitchedapps.frost.web

import android.content.Context
import android.webkit.JavascriptInterface
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.utils.*
import io.reactivex.subjects.Subject


/**
 * Created by Allan Wang on 2017-06-01.
 */
class FrostJSI(val context: Context, val webView: FrostWebViewCore, val contextMenu: FrostWebContextMenu) {

    val headerObservable: Subject<String>? = (context as? MainActivity)?.headerBadgeObservable

    val cookies: ArrayList<CookieModel>
        get() = (context as? MainActivity)?.cookies() ?: arrayListOf()

    @JavascriptInterface
    fun loadUrl(url: String) {
        context.launchWebOverlay(url)
    }

    @JavascriptInterface
    fun reloadBaseUrl(animate: Boolean) {
        L.d("FrostJSI reload")
        webView.post {
            webView.stopLoading()
            webView.loadBaseUrl(animate)
        }
    }

    @JavascriptInterface
    fun contextMenu(url: String) {
        contextMenu.post { contextMenu.show(url) }
    }

    @JavascriptInterface
    fun loadLogin() {
        context.launchLogin(cookies, true)
    }

    @JavascriptInterface
    fun emit(flag: Int) {
        webView.post { webView.frostWebClient!!.emit(flag) }
    }

    @JavascriptInterface
    fun handleHtml(html: String) {
        webView.post { webView.frostWebClient!!.handleHtml(html) }
    }

    @JavascriptInterface
    fun handleHeader(html: String) {
        headerObservable?.onNext(html)
    }

}