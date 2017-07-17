package com.pitchedapps.frost.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Created by Allan Wang on 2017-07-13.
 */
open class BaseWebViewClient : WebViewClient() {

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse?
            = shouldFrostInterceptRequest(view, request)

}