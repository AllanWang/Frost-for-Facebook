package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.views.fadeIn
import com.pitchedapps.frost.views.snackbar
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-29.
 *
 * Courtesy of takahirom
 *
 * https://github.com/takahirom/webview-in-coordinatorlayout/blob/master/app/src/main/java/com/github/takahirom/webview_in_coodinator_layout/NestedWebView.java
 */


class LoginWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    companion object {
        const val LOGIN_URL = "https://touch.facebook.com/login"
        private val userMatcher: Regex by lazy { Regex("c_user=([0-9]*);") }
    }

    val cookieObservable = PublishSubject.create<Pair<String, String?>>()
    lateinit var loginObservable: SingleSubject<CookieModel>
    lateinit var progressObservable: Subject<Int>

    init {
        FbCookie.reset({
            cookieObservable.filter { (_, cookie) -> cookie?.contains(userMatcher) ?: false }
                    .subscribe {
                        (url, cookie) ->
                        L.d("Checking cookie for $url\n\t$cookie")
                        val id = userMatcher.find(cookie!!)?.groups?.get(1)?.value
                        if (id != null) {
                            try {
                                FbCookie.save(id.toLong())
                                //TODO proceed to next view
                                cookieObservable.onComplete()
                                loginObservable.onSuccess(CookieModel(id.toLong(), "", cookie))
                            } catch (e: NumberFormatException) {
                                //todo send report that userId has changed
                            }
                        }
                    }
            setupWebview()
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        setWebViewClient(LoginClient())
        setWebChromeClient(LoginChromeClient())
    }

    fun loadLogin() {
        loadUrl(LOGIN_URL)
    }


    inner class LoginClient : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (!url.contains(FACEBOOK_COM)) {
                view.snackbar("No longer under facebook; refreshing...")
                loadLogin()
                return
            }
            cookieObservable.onNext(Pair(url, CookieManager.getInstance().getCookie(url)))
            CssAssets.LOGIN.inject(view, {
                if (view.visibility == View.INVISIBLE)
                    view.fadeIn(offset = 150L)
            })
        }
    }

    inner class LoginChromeClient : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            L.d("Login Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}")
            return super.onConsoleMessage(consoleMessage)
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressObservable.onNext(newProgress)
        }
    }
}