package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.JsBuilder
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import java.util.concurrent.TimeUnit

@SuppressLint("ViewConstructor")
/**
 * Created by Allan Wang on 2017-06-25.
 *
 * A bare bone search view meant solely to extract data from the web
 * This should be hidden
 */
class FrostWebViewSearch(context: Context, val contract: SearchContract) : WebView(context) {

    val searchSubject = PublishSubject.create<String>()

    init {
//        gone()
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = FrostWebViewClientSearch()
        addJavascriptInterface(SearchJSI(), "Frost")
        searchSubject.debounce(200, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread())
                .map {
                    Jsoup.parse(it).select("a:not([rel*='keywords(']):not([href=#])[rel]").map {
                        element ->
//                        L.d("Search element ${element.text()} ${element.textNodes().size} ${element.attr("href")}")
                        Pair(element.textNodes(), element.attr("href"))
                    }.filter { it.first.isNotEmpty() }
                }
                .subscribe {
                    content: List<Pair<List<TextNode>, String>> ->
                    content.forEach {
//                        L.e("Search result ${it.second}")
                    }
                    contract.emitSearchResponse()
                }
        reload()
        Handler().postDelayed({
            query("hi")
        }, 5000)
    }

    override fun reload() {
        super.loadUrl(FbTab.SEARCH.url)
    }

    fun query(input: String) {
        JsBuilder().js("var input=document.getElementById('main-search-input');input.click(),input.value='$input';").build().inject(this) {
            L.d("Searching for $input")
        }
    }

    /**
     * Created by Allan Wang on 2017-05-31.
     *
     * Barebones client that does what [FrostWebViewSearch] needs
     */
    inner class FrostWebViewClientSearch : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            L.i("Search Page finished $url")
            view.jsInject(JsAssets.SEARCH)
        }
    }

    inner class SearchJSI {
        @JavascriptInterface
        fun handleHtml(html: String) {
//            L.d("Search received response $html")
            searchSubject.onNext(html)
        }

        @JavascriptInterface
        fun emit(flag: Int) {
            L.d("Search flag")
            when (flag) {
                0 -> {
                    JsBuilder().js("document.getElementById('main-search-input').click()").build().inject(this@FrostWebViewSearch) {
                        L.d("Search click")
                    }
                }
                1 -> { //something is not found in the search view; this is effectively useless
                    L.d("Search subject error; reverting to full overlay")
                    searchSubject.onComplete()
                    contract.searchOverlayError()
                }
            }
        }
    }

    interface SearchContract {
        fun searchOverlayError()
        //todo add args
        fun emitSearchResponse()
    }
}



