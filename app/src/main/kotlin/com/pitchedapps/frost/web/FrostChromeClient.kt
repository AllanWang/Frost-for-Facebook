package com.pitchedapps.frost.web

import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import ca.allanwang.kau.utils.snackbar
import com.pitchedapps.frost.contracts.ActivityWebContract
import com.pitchedapps.frost.utils.L
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject


/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostChromeClient(webCore: FrostWebViewCore) : WebChromeClient() {

    val progressObservable: Subject<Int> = webCore.progressObservable
    val titleObservable: BehaviorSubject<String> = webCore.titleObservable
    val activityContract = (webCore.context as? ActivityWebContract)

    companion object {
        val consoleBlacklist = setOf(
                "edge-chat"
        )
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        if (consoleBlacklist.any { consoleMessage.message().contains(it) }) return true
        L.i("Chrome Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}")
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
        activityContract?.openFileChooser(filePathCallback, fileChooserParams) ?: webView.snackbar("File chooser not found")
        return activityContract != null
    }

}