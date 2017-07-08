package com.pitchedapps.frost.web

import android.content.Context
import android.webkit.JavascriptInterface
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.*
import io.reactivex.subjects.Subject
import jp.wasabeef.blurry.Blurry
import android.view.ViewGroup




/**
 * Created by Allan Wang on 2017-06-01.
 */
class FrostJSI(val context: Context, val webView: FrostWebViewCore) {

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
    fun contextMenu(url: String, text:String?) {
        Blurry.with(context).radius(25).sampling(2).onto(webView)
        webView.post {
            webView.context.showWebContextMenu(WebContext(url.formattedFbUrl, text))
        }
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