package com.pitchedapps.frost.injectors

import android.webkit.WebView

/**
 * Created by Allan Wang on 2017-05-31.
 */
enum class JsActions(body: String) : InjectorContract {
    /**
     * Redirects to login activity if create account is found
     * see [com.pitchedapps.frost.web.FrostJSI.loadLogin]
     */
    LOGIN_CHECK("document.getElementById('signup-button')&&Frost.loadLogin();"),
    EMPTY("");

    val function = "!function(){$body}();"

    override fun inject(webView: WebView, callback: ((String) -> Unit)?) = JsInjector(function).inject(webView, callback)
}