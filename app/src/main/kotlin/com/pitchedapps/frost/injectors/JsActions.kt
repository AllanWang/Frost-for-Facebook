package com.pitchedapps.frost.injectors

import android.webkit.WebView

/**
 * Created by Allan Wang on 2017-05-31.
 */
enum class JsActions(body: String) {
    /**
     * Redirects to login activity if create account is found
     * see [com.pitchedapps.frost.web.FrostJSI.loadLogin]
     */
    LOGIN_CHECK("document.getElementById('signup-button')&&Frost.loadLogin();");

    val function = "!function(){$body}();"

    fun inject(webView: WebView, callback: ((String) -> Unit)? = null) = JsInjector(function).inject(webView, callback)
}