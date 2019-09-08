/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.launchMain
import com.pitchedapps.frost.db.CookieEntity
import com.pitchedapps.frost.facebook.FB_LOGIN_URL
import com.pitchedapps.frost.facebook.FB_USER_MATCHER
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.USER_AGENT
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.injectors.CssHider
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.isFacebookUrl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope

/**
 * Created by Allan Wang on 2017-05-29.
 */
class LoginWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val completable: CompletableDeferred<CookieEntity> = CompletableDeferred()
    private lateinit var progressCallback: (Int) -> Unit

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = LoginClient()
        webChromeClient = LoginChromeClient()
    }

    suspend fun loadLogin(progressCallback: (Int) -> Unit): CompletableDeferred<CookieEntity> =
        coroutineScope {
            this@LoginWebView.progressCallback = progressCallback
            L.d { "Begin loading login" }
            launchMain {
                FbCookie.reset()
                setupWebview()
                loadUrl(FB_LOGIN_URL)
            }
            completable
        }

    private inner class LoginClient : BaseWebViewClient() {

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            val cookie = checkForLogin(url)
            if (cookie != null)
                completable.complete(cookie)
            if (!view.isVisible) view.fadeIn()
        }

        fun checkForLogin(url: String?): CookieEntity? {
            if (!url.isFacebookUrl) return null
            val cookie = CookieManager.getInstance().getCookie(url) ?: return null
            L.d { "Checking cookie for login" }
            val id = FB_USER_MATCHER.find(cookie)[1]?.toLong() ?: return null
            return CookieEntity(id, null, cookie)
        }

        override fun onPageCommitVisible(view: WebView, url: String?) {
            super.onPageCommitVisible(view, url)
            L.d { "Login page commit visible" }
            view.setBackgroundColor(Color.TRANSPARENT)
            if (url.isFacebookUrl)
                view.jsInject(
                    CssHider.CORE,
                    Prefs.themeInjector
                )
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            //For now, we will ignore all attempts to launch external apps during login
            if (request.url == null || request.url.scheme == "intent" || request.url.scheme == "android-app")
                return true
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    private inner class LoginChromeClient : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            L.v { "Login Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}" }
            return true
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressCallback(newProgress)
        }
    }
}
