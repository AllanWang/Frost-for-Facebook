package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.isVisible
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.CssHider
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.isFacebookUrl
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by Allan Wang on 2017-05-29.
 */
class LoginWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    companion object {
        const val LOGIN_URL = "${FB_URL_BASE}login"
        private val userMatcher: Regex = Regex("c_user=([0-9]*);")
    }

    private lateinit var loginCallback: (CookieModel) -> Unit
    private lateinit var progressCallback: (Int) -> Unit

    init {
        FbCookie.reset { setupWebview() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = LoginClient()
        webChromeClient = LoginChromeClient()
    }

    fun loadLogin(progressCallback: (Int) -> Unit, loginCallback: (CookieModel) -> Unit) {
        this.progressCallback = progressCallback
        this.loginCallback = loginCallback
        loadUrl(LOGIN_URL)
    }

    private inner class LoginClient : BaseWebViewClient() {

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            checkForLogin(url) { id, cookie -> loginCallback(CookieModel(id, "", cookie)) }
            if (url.isFacebookUrl)
                view.jsInject(CssHider.HEADER,
                        CssHider.CORE,
                        Prefs.themeInjector,
                        callback = { if (!view.isVisible) view.fadeIn(offset = WEB_LOAD_DELAY) })
            else if (!view.isVisible) view.fadeIn()
        }

        fun checkForLogin(url: String?, onFound: (id: Long, cookie: String) -> Unit) {
            doAsync {
                if (!url.isFacebookUrl) return@doAsync
                val cookie = CookieManager.getInstance().getCookie(url) ?: return@doAsync
                L.d("Checking cookie for login", cookie)
                val id = userMatcher.find(cookie)?.groups?.get(1)?.value?.toLong() ?: return@doAsync
                uiThread { onFound(id, cookie) }
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            //For now, we will ignore all attempts to launch external apps during login
            if (request.url == null || request.url.scheme == "intent" || request.url.scheme == "android-app")
                return true
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    inner class LoginChromeClient : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            L.d("Login Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}")
            return true
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressCallback(newProgress)
        }
    }
}