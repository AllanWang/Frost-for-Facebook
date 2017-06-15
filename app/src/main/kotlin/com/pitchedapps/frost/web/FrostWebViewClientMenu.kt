package com.pitchedapps.frost.web

import android.graphics.Bitmap
import android.webkit.WebView
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.utils.L
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostWebViewClientMenu(refreshObservable: Subject<Boolean>) : FrostWebViewClient(refreshObservable) {

    var content: String? = null
    var view: FrostWebViewCore? = null

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (content != null) {
            when (url) {
                "https://m.facebook.com/settings",
                "https://m.facebook.com/settings#",
                "https://m.facebook.com/settings#!/settings?soft=bookmarks" -> {
                    L.d("Load from stored $url")
                    view.stopLoading()
                    view.loadDataWithBaseURL("https://touch.facebook.com/notifications", content, "text/html", "utf-8", "https://google.ca/test")
                }
            }
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        with(view as FrostWebViewCore) {
            if (url == view.baseUrl) {
                this@FrostWebViewClientMenu.view = view
                inject(JsAssets.MENU, view, {
                    inject(JsAssets.MENU_CLICK, view) //menu injection must be after or we will have a loop from the click listener
                })
            } else {
                inject(JsAssets.MENU_CLICK, view)
            }
        }
    }

    override fun emit(flag: Int) {
        super.emit(flag)
        if (view != null) super.onPageFinishedReveal(view!!, true)
        view = null
    }

    override fun onPageFinishedReveal(view: FrostWebViewCore, url: String?) {
        when (url) {
            "https://m.facebook.com/settings",
            "https://m.facebook.com/settings#",
            "https://m.facebook.com/settings#!/settings?soft=bookmarks" -> {
                //do nothing; we will further inject before revealing
            }
            else -> super.onPageFinishedReveal(view, false)
        }
    }

    override fun handleHtml(html: String) {
        super.handleHtml(html)
        content = html //we will not save this locally in case things change
    }
}