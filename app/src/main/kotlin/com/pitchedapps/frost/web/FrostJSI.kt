package com.pitchedapps.frost.web

import android.content.Context
import android.webkit.JavascriptInterface
import ca.allanwang.kau.utils.startActivity
import com.pitchedapps.frost.activities.ImageActivity
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.formattedFbUrl
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

    @JavascriptInterface
    fun loadUrl(url: String) {
        context.launchWebOverlay(url)
    }

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
        webView.post { context.showWebContextMenu(WebContext(url.formattedFbUrl, text)) }
    }

    /**
     * Get notified when a stationary long click starts or ends
     * This will be used to toggle the main activities viewpager swipe
     */
    @JavascriptInterface
    fun longClick(start: Boolean) {
        activity?.viewPager?.enableSwipe = !start
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
    fun handleHtml(html: String) {
        webView.post { webView.frostWebClient.handleHtml(html) }
    }

    @JavascriptInterface
    fun handleHeader(html: String) {
        headerObservable?.onNext(html)
    }

}