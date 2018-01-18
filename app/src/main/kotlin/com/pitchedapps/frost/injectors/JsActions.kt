package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.facebook.FB_URL_BASE

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Collection of short js functions that are embedded directly
 */
enum class JsActions(body: String) : InjectorContract {
    /**
     * Redirects to login activity if create account is found
     * see [com.pitchedapps.frost.web.FrostJSI.loadLogin]
     */
    LOGIN_CHECK("document.getElementById('signup-button')&&Frost.loadLogin();"),
    BASE_HREF("""document.write("<base href='$FB_URL_BASE'/>");"""),
    FETCH_BODY("""setTimeout(function(){var e=document.querySelector("main");e||(e=document.querySelector("body")),Frost.handleHtml(e.outerHTML)},1e2);"""),
    /**
     * Used as a pseudoinjector for maybe functions
     */
    EMPTY("");

    val function = "!function(){$body}();"

    override fun inject(webView: WebView, callback: (() -> Unit)?) =
            JsInjector(function).inject(webView, callback)
}