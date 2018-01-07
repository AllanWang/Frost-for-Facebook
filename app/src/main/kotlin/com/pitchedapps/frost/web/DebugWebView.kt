package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.pitchedapps.frost.facebook.FB_USER_MATCHER
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.injectors.CssHider
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.isFacebookUrl
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by Allan Wang on 2018-01-05.
 *
 * A barebone webview with a refresh listener
 */
class DebugWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var onPageFinished: (String?) -> Unit = {}

    init {
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = DebugClient()
    }

    private inner class DebugClient : BaseWebViewClient() {

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            onPageFinished(url)
        }

    }

}