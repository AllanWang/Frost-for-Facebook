package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.v4.view.NestedScrollingChild
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import ca.allanwang.kau.utils.*
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Created by Allan Wang on 2017-06-25.
 *
 * A bare bone search view meant solely to extract data from the web
 * This should be hidden
 */
class FrostWebViewSearch (context: Context) : WebView(context) {
    var baseUrl: String? = null
    var baseEnum: FbTab? = null
    internal var frostWebClient: FrostWebViewClient? = null

    init {
        gone()
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview(url: String, enum: FbTab? = null) {
        baseUrl = url
        baseEnum = enum
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
//        settings.domStorageEnabled = true
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        frostWebClient = baseEnum?.webClient?.invoke(this) ?: FrostWebViewClient(this)
        webViewClient = frostWebClient
        webChromeClient = FrostChromeClient(this)
        addJavascriptInterface(FrostJSI(context, this), "Frost")
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun loadUrl(url: String?, animate: Boolean) {
        if (url == null) return
        registerTransition(animate)
        super.loadUrl(url)
    }

    fun reload(animate: Boolean) {
        registerTransition(animate)
        super.reload()
    }

    /**
     * Hook onto the refresh observable for one cycle
     * Animate toggles between the fancy ripple and the basic fade
     * The cycle only starts on the first load since there may have been another process when this is registered
     */
    fun registerTransition(animate: Boolean) {
        var dispose: Disposable? = null
        var loading = false
        dispose = refreshObservable.subscribeOn(AndroidSchedulers.mainThread()).subscribe {
            if (it) {
                loading = true
                if (isVisible()) fadeOut(duration = 200L)
            } else if (loading) {
                dispose?.dispose()
                if (animate && Prefs.animate) circularReveal(offset = 150L)
                else fadeIn(duration = 100L)
            }
        }
    }

}
