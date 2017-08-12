package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import ca.allanwang.kau.utils.gone
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.injectors.InjectorContract
import com.pitchedapps.frost.utils.L
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Allan Wang on 2017-08-12.
 */
const val HTML_EXTRACTOR_SUCCESS = 0
const val HTML_EXTRACTOR_CANCELLED = 1
const val HTML_EXTRACTOR_ERROR = 2
const val HTML_EXTRACTOR_TIMEOUT = 3

fun Context.launchHeadlessHtmlExtractor(url: String, injector: InjectorContract, action: (Single<Pair<String, Int>>) -> Unit) {
    val single = Single.create<Pair<String, Int>> { e: SingleEmitter<Pair<String, Int>> ->
        HeadlessHtmlExtractor(this, url, injector, e)
        e.setCancellable { e.onSuccess("" to HTML_EXTRACTOR_CANCELLED) }
    }.subscribeOn(AndroidSchedulers.mainThread()).observeOn(Schedulers.io())
            .timeout(20, TimeUnit.SECONDS, Schedulers.io(), { it.onSuccess("" to HTML_EXTRACTOR_TIMEOUT) })
            .onErrorReturn { "" to HTML_EXTRACTOR_ERROR }
    action(single)
}

/**
 * Given a link and some javascript, will load the link and load the JS on completion
 * The JS is expected to call [HeadlessHtmlExtractor.HtmlJSI.handleHtml], which will be sent
 * to the [emitter]
 */
@SuppressLint("ViewConstructor")
private class HeadlessHtmlExtractor(
        context: Context, url: String, val injector: InjectorContract, val emitter: SingleEmitter<Pair<String, Int>>
) : WebView(context) {

    val startTime = System.currentTimeMillis()

    init {
        L.v("Created HeadlessHtmlExtractor for $url")
        gone()
        setupWebview(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebview(url: String) {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
        webViewClient = HeadlessWebViewClient(url, injector) // basic client that loads our JS once the page has loaded
        webChromeClient = QuietChromeClient() // basic client that disables logging
        addJavascriptInterface(HtmlJSI(), "Frost")
        loadUrl(url)
    }

    inner class HtmlJSI {
        @JavascriptInterface
        fun handleHtml(html: String?) {
            val time = System.currentTimeMillis() - startTime
            L.d("HeadlessHtmlExtractor fetched $url in $time ms")
            emitter.onSuccess((html ?: "") to HTML_EXTRACTOR_SUCCESS)
            post {
                settings.javaScriptEnabled = false
                settings.blockNetworkLoads = true
                destroy()
            }
        }
    }
}