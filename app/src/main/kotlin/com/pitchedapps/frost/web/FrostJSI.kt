package com.pitchedapps.frost.web

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.webkit.JavascriptInterface
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.utils.*
import io.reactivex.subjects.Subject


/**
 * Created by Allan Wang on 2017-06-01.
 */
class FrostJSI(val webView: FrostWebViewCore) {

    val context: Context
        get() = webView.context

    val activity: MainActivity?
        get() = (context as? MainActivity)

    val headerObservable: Subject<String>? = activity?.headerBadgeObservable

    val cookies: ArrayList<CookieModel>
        get() = activity?.cookies() ?: arrayListOf()

    /**
     * Attempts to load the url in an overlay
     * Returns {@code true} if successful, meaning the event is consumed,
     * or {@code false} otherwise, meaning the event should be propagated
     */
    @JavascriptInterface
    fun loadUrl(url: String?): Boolean
            = if (url == null) false else context.requestWebOverlay(url)

    @JavascriptInterface
    fun reloadBaseUrl(animate: Boolean) {
        L.d("FrostJSI reload")
        webView.post {
            webView.stopLoading()
            webView.loadBaseUrl(animate)
        }
    }

    @JavascriptInterface
    fun contextMenu(url: String, text: String?) {
        //url will be formatted through webcontext
        webView.post { context.showWebContextMenu(WebContext(url, text)) }
    }

    /**
     * Get notified when a stationary long click starts or ends
     * This will be used to toggle the main activities viewpager swipe
     */
    @JavascriptInterface
    fun longClick(start: Boolean) {
        activity?.viewPager?.enableSwipe = !start
    }

    /**
     * Allow or disallow the pull down to refresh action
     */
    @JavascriptInterface
    fun disableSwipeRefresh(disable: Boolean) {
        webView.post { (webView.parent as? SwipeRefreshLayout)?.isEnabled = !disable }
    }

    @JavascriptInterface
    fun loadLogin() {
        context.launchLogin(cookies, true)
    }

    /**
     * Launch image overlay
     */
    @JavascriptInterface
    fun loadImage(imageUrl: String, text: String?) {
        context.launchImageActivity(imageUrl, text)
    }

    @JavascriptInterface
    fun emit(flag: Int) {
        webView.post { webView.frostWebClient.emit(flag) }
    }

    @JavascriptInterface
    fun handleHtml(html: String?) {
        html ?: return
        webView.post { webView.frostWebClient.handleHtml(html) }
    }

    @JavascriptInterface
    fun handleHeader(html: String?) {
        html ?: return
        headerObservable?.onNext(html)
    }

}