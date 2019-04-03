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
package com.pitchedapps.frost.activities

import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.HttpAuthHandler
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L
import kotlinx.android.synthetic.main.activity_test.*

/**
 * Created by Allan Wang on 2017-06-01.
 */
class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        L.e { "Default UA ${WebSettings.getDefaultUserAgent(this)}" }
        L.e { "Current UA ${webview.settings.userAgentString}" }
//        webview.settings.userAgentString = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
        webview.loadUrl("https://m.facebook.com/story.php?story_fbid=2124253044331708&id=104958162837")
        webview.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = true
            }
            webChromeClient = A()
            webViewClient = B()
        }
    }

    class B : WebViewClient() {
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            L.e { "Received error" }
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpAuthRequest(
            view: WebView?,
            handler: HttpAuthHandler?,
            host: String?,
            realm: String?
        ) {
            L.e { "Received onReceivedHttpAuthRequest" }
            super.onReceivedHttpAuthRequest(view, handler, host, realm)
        }
    }

    class A : WebChromeClient() {
        override fun onJsPrompt(
            view: WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult?
        ): Boolean {
            L.e { "JS prompt $message" }
            return super.onJsPrompt(view, url, message, defaultValue, result)
        }

        override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            L.e { "JS confirm $message" }
            return super.onJsConfirm(view, url, message, result)
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            L.e { "JS onCreateWindow" }
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            L.e { "JS onPermissionRequest" }
            super.onPermissionRequest(request)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            L.e { "JS onConsoleMessage ${consoleMessage?.message()}" }
            return super.onConsoleMessage(consoleMessage)
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            L.e { "JS onShowCustomView" }
            super.onShowCustomView(view, callback)
        }

        override fun onRequestFocus(view: WebView?) {
            L.e { "JS onRequestFocus" }
            super.onRequestFocus(view)
        }

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            L.e { "JS onJsAlert $message" }
            return super.onJsAlert(view, url, message, result)
        }
    }
}
