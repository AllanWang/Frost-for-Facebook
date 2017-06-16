package com.pitchedapps.frost.web

import android.graphics.Bitmap
import android.webkit.WebView
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostWebViewClientMenu(webCore: FrostWebViewCore) : FrostWebViewClient(webCore) {

    var content: String? = null
    val progressObservable: Subject<Int> = webCore.progressObservable
    private val contentBaseUrl = "https://touch.facebook.com/notifications"

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (content != null) {
            when (url.removePrefix(FB_URL_BASE)) {
                "settings",
                "settings#",
                "settings#!/settings?soft=bookmarks" -> {
                    L.d("Load from stored $url")
                    view.stopLoading()
                    view.loadDataWithBaseURL(contentBaseUrl, content, "text/html", "utf-8", "https://google.ca/test")
                }
            }
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (url == webCore.baseUrl && content == null) {
            jsInject(JsAssets.MENU, callback = {
                jsInject(JsAssets.MENU_CLICK) //menu injection must be after or we will have a loop from the click listener
            })
        } else if (url == contentBaseUrl) jsInject(JsAssets.MENU_CLICK)
    }

    override fun emit(flag: Int) {
        super.emit(flag)
        super.injectAndFinish()
    }

    override fun onPageFinishedActions(url: String?) {
        when (url?.removePrefix(FB_URL_BASE)) {
            "settings",
            "settings#",
            "settings#!/settings?soft=bookmarks" -> {
                //do nothing; we will further inject before revealing
            }
            else -> injectAndFinish()
        }
    }

    override fun handleHtml(html: String) {
        super.handleHtml(html)
        content = html //we will not save this locally in case things change
    }
}