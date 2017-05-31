package com.pitchedapps.frost.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.UrlQuerySanitizer
import android.util.AttributeSet
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.facebook.AccessToken
import com.pitchedapps.frost.facebook.FB_KEY
import com.pitchedapps.frost.facebook.retro.FrostApi.frostApi
import com.pitchedapps.frost.facebook.retro.Me
import com.pitchedapps.frost.facebook.retro.enqueueFrost
import com.pitchedapps.frost.utils.L
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Allan Wang on 2017-05-29.
 */
class LoginWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        setWebViewClient(object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                L.e("Error ${request}")
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                L.d("Loading $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url == null) return
                val sanitizer = UrlQuerySanitizer(url)
                val accessToken = sanitizer.getValue("access_token")
                val expiresIn = sanitizer.getValue("expires_in")
                val grantedScopes = sanitizer.getValue("granted_scopes")
                val deniedScopes = sanitizer.getValue("deniedScopes")


                L.d("Loaded $url")
            }
        })
    }

    fun saveAccessToken(accessToken: String, expiresIn: String, grantedScopes: String?, deniedScopes: String?) {
        L.d("Granted $grantedScopes")
        L.d("Denied $deniedScopes")
        frostApi.me().enqueueFrost { call, response ->

        }

    }

}
