package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import ca.allanwang.kau.utils.gone
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.injectors.InjectorContract
import com.pitchedapps.frost.utils.L
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.runOnUiThread
import java.util.concurrent.TimeUnit

/**
 * Created by Allan Wang on 2017-08-12.
 *
 * Launches a headless html request and returns a result pair
 * When successful, the pair will contain the html content and -1
 * When unsuccessful, the pair will contain an empty string and a StringRes for the given error
 *
 * All errors are rerouted to success calls, so no exceptions should occur.
 * The headless extractor will also destroy itself on cancellation or when the request is finished
 */
fun Context.launchHeadlessHtmlExtractor(url: String, injector: InjectorContract, action: (Single<Pair<String, Int>>) -> Unit) {
    val single = Single.create<Pair<String, Int>> { e: SingleEmitter<Pair<String, Int>> ->
        val extractor = HeadlessHtmlExtractor(this, url, injector, e)
        e.setCancellable {
            runOnUiThread { extractor.destroy() }
            e.onSuccess("" to R.string.debug_request_cancelled)
        }
    }.subscribeOn(AndroidSchedulers.mainThread())
            .timeout(20, TimeUnit.SECONDS, Schedulers.io(), { it.onSuccess("" to R.string.debug_request_timeout) })
            .onErrorReturn { "" to R.string.debug_request_error }
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
            emitter.onSuccess((html ?: "") to -1)
            post {
                L.d("HeadlessHtmlExtractor fetched $url in $time ms")
                settings.javaScriptEnabled = false
                settings.blockNetworkLoads = true
                destroy()
            }
        }
    }

    override fun destroy() {
        super.destroy()
        L.d("HeadlessHtmlExtractor destroyed")
    }
}