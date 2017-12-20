package com.pitchedapps.frost.web

import android.net.Uri
import android.webkit.*
import ca.allanwang.kau.permissions.PERMISSION_ACCESS_FINE_LOCATION
import ca.allanwang.kau.permissions.kauRequestPermissions
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.ActivityContract
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostSnackbar
import com.pitchedapps.frost.views.FrostWebView
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject


/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Collection of chrome clients
 */

/**
 * Fully quiet client that disables any prompts relating to the UI
 * (as nothing is attached)
 */
class HeadlessChromeClient : WebChromeClient() {

    override fun onConsoleMessage(consoleMessage: ConsoleMessage) = true

    override fun onJsAlert(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
        result.cancel()
        return true
    }

    override fun onJsConfirm(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
        result.cancel()
        return true
    }

    override fun onJsPrompt(view: WebView, url: String?, message: String?, defaultValue: String?, result: JsPromptResult): Boolean {
        result.cancel()
        return true
    }
}

/**
 * The default chrome client
 */
class FrostChromeClient(web: FrostWebView) : WebChromeClient() {

    private val progress: Subject<Int> = web.progressObservable
    private val title: BehaviorSubject<String> = web.titleObservable
    private val activity = (web.context as? ActivityContract)
    private val context = web.context!!

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        if (consoleBlacklist.any { consoleMessage.message().contains(it) }) return true
        L.d("Chrome Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}")
        return true
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        if (title.contains("http") || this.title.value == title) return
        this.title.onNext(title)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progress.onNext(newProgress)
    }

    override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>?>, fileChooserParams: FileChooserParams): Boolean {
        activity?.openFileChooser(filePathCallback, fileChooserParams) ?: webView.frostSnackbar(R.string.file_chooser_not_found)
        return activity != null
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        L.i("Requesting geolocation")
        context.kauRequestPermissions(PERMISSION_ACCESS_FINE_LOCATION) { granted, _ ->
            L.i("Geolocation response received; ${if (granted) "granted" else "denied"}")
            callback(origin, granted, true)
        }
    }


}