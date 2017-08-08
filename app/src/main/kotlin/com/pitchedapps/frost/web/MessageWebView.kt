package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.webkit.JavascriptInterface
import android.webkit.WebView
import ca.allanwang.kau.utils.gone
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.services.NotificationService
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostAnswersCustom
import org.jetbrains.anko.doAsync

/**
 * Created by Allan Wang on 2017-07-17.
 *
 * Bare boned headless view made solely to extract conversation info
 */
@SuppressLint("ViewConstructor")
class MessageWebView(val service: NotificationService, val params: JobParameters?, val cookie: CookieModel) : WebView(service) {

    private val startTime = System.currentTimeMillis()
    private var isCancelled = false

    init {
        gone()
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
        webViewClient = HeadlessWebViewClient("MessageNotifs", JsAssets.NOTIF_MSG)
        webChromeClient = QuietChromeClient()
        addJavascriptInterface(MessageJSI(), "Frost")
        loadUrl(FbTab.MESSAGES.url)
    }

    fun finish() {
        if (isCancelled) return
        isCancelled = true
        post { destroy() }
        service.finish(params)
    }

    override fun destroy() {
        L.d("MessageWebView destroyed")
        super.destroy()
    }

    inner class MessageJSI {
        @JavascriptInterface
        fun handleHtml(html: String?) {
            html ?: return
            if (isCancelled) return
            if (html.length < 10) return finish()
            val time = System.currentTimeMillis() - startTime
            L.d("Notif messages fetched in $time ms")
            frostAnswersCustom("NotificationTime", "Type" to "IM Headless", "Duration" to time)
            doAsync { service.fetchMessageNotifications(cookie, html); finish() }
        }
    }

}