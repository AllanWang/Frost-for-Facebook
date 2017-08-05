package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.isVisible
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.injectors.CssHider
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-29.
 */
class LoginWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    companion object {
        const val LOGIN_URL = "https://touch.facebook.com/login"
        private val userMatcher: Regex = Regex("c_user=([0-9]*);")
    }

    val cookieObservable = PublishSubject.create<String>()!!
    lateinit var loginObservable: SingleSubject<CookieModel>
    lateinit var progressObservable: Subject<Int>

    init {
        FbCookie.reset({
            cookieObservable.map { CookieManager.getInstance().getCookie(it) ?: "" }
                    .filter { it.contains(userMatcher) }
                    .subscribe {
                        cookie ->
                        L.d("Checking cookie for login", cookie)
                        val id = userMatcher.find(cookie)!!.groups[1]!!.value.toLong()
                        FbCookie.save(id)
                        cookieObservable.onComplete()
                        loginObservable.onSuccess(CookieModel(id, "", cookie))
                    }
            setupWebview()
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = LoginClient()
        webChromeClient = LoginChromeClient()
    }

    fun loadLogin() {
        loadUrl(LOGIN_URL)
    }

    inner class LoginClient : BaseWebViewClient() {

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
//            if (url == null || (!url.contains(FACEBOOK_COM) && !url.contains("intent"))) {
//                view.frostSnackbar(R.string.no_longer_facebook)
//                loadLogin()
//                return
//            }
            val containsFacebook = url?.contains(FACEBOOK_COM) ?: false
            if (containsFacebook)
                cookieObservable.onNext(url!!)
            view.jsInject(CssHider.HEADER.maybe(containsFacebook),
                    CssHider.CORE.maybe(containsFacebook),
                    Prefs.themeInjector.maybe(containsFacebook),
                    callback = { if (!view.isVisible) view.fadeIn(offset = 150L) })
        }
    }

    inner class LoginChromeClient : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            L.d("Login Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}")
            return true
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressObservable.onNext(newProgress)
        }
    }
}