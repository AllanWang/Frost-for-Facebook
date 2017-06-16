package com.pitchedapps.frost.web

import android.content.Context
import android.webkit.JavascriptInterface
import com.pitchedapps.frost.LoginActivity
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.SelectorActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.utils.launchWebOverlay


/**
 * Created by Allan Wang on 2017-06-01.
 */
class FrostJSI(val context: Context, val webView: FrostWebViewCore) {
    val cookies: ArrayList<CookieModel>
        get() = (context as? MainActivity)?.cookies() ?: arrayListOf()

    var lastUrl: String = ""

    @JavascriptInterface
    fun loadUrl(url: String) {
        if (url != lastUrl) {
            lastUrl = url
            context.launchWebOverlay(url)
        }
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
    fun loadLogin() {
        if (cookies.isNotEmpty())
            context.launchNewTask(SelectorActivity::class.java, cookies)
        else
            context.launchNewTask(LoginActivity::class.java)
    }

    @JavascriptInterface
    fun emit(flag: Int) {
        webView.post { webView.frostWebClient!!.emit(flag) }
    }

    @JavascriptInterface
    fun handleHtml(html: String) {
        webView.post { webView.frostWebClient!!.handleHtml(html) }
    }

}