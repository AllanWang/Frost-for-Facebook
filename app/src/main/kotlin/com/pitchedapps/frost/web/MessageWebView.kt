package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.webkit.JavascriptInterface
import android.webkit.WebView
import ca.allanwang.kau.utils.gone
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.services.NotificationService
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostAnswersCustom
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread

@SuppressLint("ViewConstructor")
/**
 * Created by Allan Wang on 2017-07-17.
 *
 * Bare boned headless view made solely to extract conversation info
 */
class MessageWebView(val service: NotificationService, val params: JobParameters?) : WebView(service) {

    init {
        gone()
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
        webViewClient = HeadlessWebViewClient("MessageNotifs", JsActions.GET_MESSAGES)
        webChromeClient = QuietChromeClient()
        addJavascriptInterface(MessageJSI(), "Frost")
    }

    private val startTime = System.currentTimeMillis()
    private val endTime: Long by lazy { System.currentTimeMillis() }
    private var inProgress = false
    private val pendingRequests: MutableList<CookieModel> = mutableListOf()
    private lateinit var data: CookieModel

    fun request(data: CookieModel) {
        pendingRequests.add(data)
        if (inProgress) return
        inProgress = true
        load(data)
    }

    private fun load(data: CookieModel) {
        L.d("Notif retrieving messages", data.toString())
        this.data = data
        FbCookie.setWebCookie(data.cookie) { context.runOnUiThread { L.d("Notif messages load"); loadUrl(FbTab.MESSAGES.url) } }
    }

    inner class MessageJSI {
        @JavascriptInterface
        fun handleHtml(html: String) {
            L.d("Notif messages received", data.toString())
            doAsync { service.fetchMessageNotifications(data, html) }
            pendingRequests.remove(data)
            if (pendingRequests.isEmpty()) {
                val time = endTime - startTime
                L.d("Notif messages finished $time")
                frostAnswersCustom("Notifications") {
                    putCustomAttribute("Message retrieval duration", time)
                }
                post { destroy() }
                service.jobFinished(params, false)
                service.future = null
            } else {
                load(pendingRequests.first())
            }
        }
    }

}