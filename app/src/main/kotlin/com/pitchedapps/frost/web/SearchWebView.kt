package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import ca.allanwang.kau.searchview.SearchItem
import ca.allanwang.kau.utils.gone
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.JsBuilder
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.runOnUiThread
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

@SuppressLint("ViewConstructor")
/**
 * Created by Allan Wang on 2017-06-25.
 *
 * A bare bone headless search view meant solely to extract search results from the web
 * Having a single webview allows us to avoid loading the whole page with each query
 */
class SearchWebView(context: Context, val contract: SearchContract) : WebView(context) {

    val searchSubject = PublishSubject.create<String>()!!

    init {
        gone()
        setupWebview()
    }

    /**
     * Basic info of last search results, so we can check if the list has actually changed
     * Contains the last item's href (search more) as well as the number of items found
     * This holder is synchronized
     */
    var previousResult: Pair<String, Int> = Pair("", 0)

    fun saveResultFrame(result: List<Pair<List<String>, String>>) {
        synchronized(previousResult) {
            previousResult = Pair(result.last().second, result.size)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
        webViewClient = HeadlessWebViewClient("Search", JsAssets.SEARCH)
        webChromeClient = QuietChromeClient()
        addJavascriptInterface(SearchJSI(), "Frost")
        searchSubject.debounce(300, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread())
                .map {
                    val doc = Jsoup.parse(it)
                    L.d(doc.getElementById("main-search_input")?.html())
                    val searchQuery = doc.getElementById("main-search-input")?.text() ?: "Null input"
                    L.d("Search query", searchQuery)
                    doc.select("a:not([rel*='keywords(']):not([href=#])[rel]").map {
                        element ->
                        //split text into separate items
                        L.v("Search element", element.attr("href"))
                        val texts = element.select("div").map { it.text() }.filter { !it.isNullOrBlank() }
                        val pair = Pair(texts, element.attr("href"))
                        L.v("Search element potential", pair.toString())
                        pair
                    }.filter { it.first.isNotEmpty() }
                }
                .filter { it.isNotEmpty() }
                .filter { Pair(it.last().second, it.size) != previousResult }
                .subscribe {
                    content: List<Pair<List<String>, String>> ->
                    saveResultFrame(content)
                    L.d("Search element count ${content.size}")
                    contract.emitSearchResponse(content.map {
                        (texts, href) ->
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
            context.runOnUiThread { settings.blockNetworkLoads = value }
        }

    override fun reload() {
        super.loadUrl(FbTab.SEARCH.url)
    }

    /**
     * Sets the input to have our given text, then dispatches the input event so the webpage recognizes it
     */
    fun query(input: String) {
        pauseLoad = false
        L.d("Searching attempt", input)
        JsBuilder().js("var e=document.getElementById('main-search-input');if(e){e.value='$input';var n=new Event('input',{bubbles:!0,cancelable:!0});e.dispatchEvent(n),e.dispatchEvent(new Event('focus'))}else console.log('Input field not found');").build().inject(this)
    }

    inner class SearchJSI {
        @JavascriptInterface
        fun handleHtml(html: String?) {
            html ?: return
            L.d("Search received response ${contract.isSearchOpened}")
            if (!contract.isSearchOpened) pauseLoad = true
            searchSubject.onNext(html)
        }

        @JavascriptInterface
        fun emit(flag: Int) {
            when (flag) {
                0 -> {
                    L.d("Search loaded successfully")
                }
                1 -> { //something is not found in the search view; this is effectively useless
                    L.e("Search subject error; reverting to full overlay")
                    Prefs.searchBar = false
                    searchSubject.onComplete()
                    contract.searchOverlayDispose()
                }
                2 -> {
                    L.v("Search emission received")
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
        val isSearchOpened: Boolean
    }
}



