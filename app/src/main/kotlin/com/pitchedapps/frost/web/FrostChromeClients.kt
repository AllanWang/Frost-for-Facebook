package com.pitchedapps.frost.web

import android.net.Uri
import android.webkit.*
import ca.allanwang.kau.permissions.PERMISSION_ACCESS_FINE_LOCATION
import ca.allanwang.kau.permissions.kauRequestPermissions
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.ActivityWebContract
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostSnackbar
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject


/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Collection of chrome clients
 */

/**
 * Nothing more than a client without logging
 */
class QuietChromeClient : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage) = true
}

/**
 * The default chrome client
 */
class FrostChromeClient(webCore: FrostWebViewCore) : WebChromeClient() {

    val progressObservable: Subject<Int> = webCore.progressObservable
    val titleObservable: BehaviorSubject<String> = webCore.titleObservable
    val activityContract = (webCore.context as? ActivityWebContract)
    val context = webCore.context!!

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        if (consoleBlacklist.any { consoleMessage.message().contains(it) }) return true
        L.d("Chrome Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}")
        return true
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        if (title.contains("http") || titleObservable.value == title) return
        titleObservable.onNext(title)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progressObservable.onNext(newProgress)
    }

    override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
        activityContract?.openFileChooser(filePathCallback, fileChooserParams) ?: webView.frostSnackbar(R.string.file_chooser_not_found)
        return activityContract != null
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        L.i("Requesting geolocation")
        context.kauRequestPermissions(PERMISSION_ACCESS_FINE_LOCATION) {
            granted, _ ->
            L.i("Geolocation response received; ${if (granted) "granted" else "denied"}")
            callback(origin, granted, true)
        }
    }


}