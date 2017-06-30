package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import ca.allanwang.kau.searchview.SearchItem
import ca.allanwang.kau.utils.gone
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.JsBuilder
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

@SuppressLint("ViewConstructor")
/**
 * Created by Allan Wang on 2017-06-25.
 *
 * A bare bone search view meant solely to extract data from the web
 * This should be hidden
 * Having a single webview allows us to avoid loading the whole page with each query
 */
class FrostWebViewSearch(context: Context, val contract: SearchContract) : WebView(context) {

    val searchSubject = PublishSubject.create<String>()

    init {
        gone()
        setupWebview()
    }

    /**
     * Basic info of last search results, so we can check if the list has actually changed
     * Contains the last item's href (search more) as well as the number of items found
     * This holder is synchronized
     */
    var previousResult: Pair<String?, Int> = Pair(null, 0)

    fun saveResultFrame(result: List<Pair<List<String>, String>>) {
        synchronized(previousResult) {
            previousResult = Pair(result.lastOrNull()?.second, result.size)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = FrostWebViewClientSearch()
        addJavascriptInterface(SearchJSI(), "Frost")
        searchSubject.debounce(300, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread())
                .map {
                    Jsoup.parse(it).select("a:not([rel*='keywords(']):not([href=#])[rel]").map {
                        element ->
                        //split text into separate items
                        val texts = element.select("div").map { (it.ownText()) }.filter { it.isNotBlank() }
                        Pair(texts, element.attr("href"))
                    }.filter { it.first.isNotEmpty() }
                }
                .filter { content -> Pair(content.lastOrNull()?.second, content.size) != previousResult }
                .subscribe {
                    content: List<Pair<List<String>, String>> ->
                    saveResultFrame(content)
                    contract.emitSearchResponse(content.map {
                        (texts, href) ->
                        L.d("Search element $texts $href")
                        SearchItem(href, texts[0], texts.getOrNull(1))
                    })
                }
        reload()
    }

    /**
     * Toggles web activity
     * Should be done in conjunction with showing/hiding the search view
     */
    var pauseLoad: Boolean
        get() = settings.blockNetworkLoads
        set(value) {
            settings.blockNetworkLoads = value
        }

    override fun reload() {
        super.loadUrl(FbTab.SEARCH.url)
    }

    /**
     * Sets the input to have our given text, then dispatches the input event so the webpage recognizes it
     */
    fun query(input: String) {
        L.d("Searching attempt for $input")
        JsBuilder().js("var e=document.getElementById('main-search-input');if(e){e.value='$input';var n=new Event('input',{bubbles:!0,cancelable:!0});e.dispatchEvent(n)}else console.log('Input field not found')").build().inject(this) {
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
            L.d("Search received response")
            searchSubject.onNext(html)
        }

        @JavascriptInterface
        fun emit(flag: Int) {
            when (flag) {
                0 -> {
                    L.d("Search loaded successfully")
                }
                1 -> { //something is not found in the search view; this is effectively useless
                    L.d("Search subject error; reverting to full overlay")
                    searchSubject.onComplete()
                    contract.searchOverlayDispose()
                }
            }
        }
    }

    /**
     * Clear up some components
     */
    fun dispose() {
        searchSubject.onComplete()
    }

    interface SearchContract {
        fun searchOverlayDispose()
        fun emitSearchResponse(items: List<SearchItem>)
    }
}



