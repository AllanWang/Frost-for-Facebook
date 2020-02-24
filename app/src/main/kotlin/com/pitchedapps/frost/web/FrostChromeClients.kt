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

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import ca.allanwang.kau.permissions.PERMISSION_ACCESS_FINE_LOCATION
import ca.allanwang.kau.permissions.kauRequestPermissions
import ca.allanwang.kau.utils.materialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.ActivityContract
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostSnackbar
import com.pitchedapps.frost.views.FrostWebView
import kotlinx.coroutines.channels.SendChannel

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Collection of chrome clients
 */

/**
 * The default chrome client
 */
class FrostChromeClient(web: FrostWebView) : WebChromeClient() {

    private val refresh: SendChannel<Boolean> = web.parent.refreshChannel
    private val progress: SendChannel<Int> = web.parent.progressChannel
    private val title: SendChannel<String> = web.parent.titleChannel
    private val activity = (web.context as? ActivityContract)
    private val context = web.context!!

    override fun getDefaultVideoPoster(): Bitmap? =
        super.getDefaultVideoPoster()
            ?: Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)

    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        L.v { "Chrome Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}" }
        return true
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        if (title.startsWith("http")) return
        this.title.offer(title)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        progress.offer(newProgress)
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>?>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        activity?.openFileChooser(filePathCallback, fileChooserParams)
            ?: webView.frostSnackbar(R.string.file_chooser_not_found)
        return activity != null
    }

    private fun JsResult.frostCancel() {
        cancel()
        refresh.offer(false)
        progress.offer(100)
    }

    override fun onJsAlert(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        view.context.materialDialog {
            title(text = url)
            message(text = message)
            positiveButton { result.confirm() }
            onDismiss { result.frostCancel() }
        }
        return true
    }

    override fun onJsConfirm(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        view.context.materialDialog {
            title(text = url)
            message(text = message)
            positiveButton { result.confirm() }
            negativeButton { result.frostCancel() }
            onDismiss { result.frostCancel() }
        }
        return true
    }

    override fun onJsBeforeUnload(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        view.context.materialDialog {
            title(text = url)
            message(text = message)
            positiveButton { result.confirm() }
            negativeButton { result.frostCancel() }
            onDismiss { result.frostCancel() }
        }
        return true
    }

    override fun onJsPrompt(
        view: WebView,
        url: String,
        message: String,
        defaultValue: String?,
        result: JsPromptResult
    ): Boolean {
        view.context.materialDialog {
            title(text = url)
            message(text = message)
            input(prefill = defaultValue) { _, charSequence ->
                result.confirm(charSequence.toString())
            }
            // positive button added through input
            negativeButton { result.frostCancel() }
            onDismiss { result.frostCancel() }
        }
        return true
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String,
        callback: GeolocationPermissions.Callback
    ) {
        L.i { "Requesting geolocation" }
        context.kauRequestPermissions(PERMISSION_ACCESS_FINE_LOCATION) { granted, _ ->
            L.i { "Geolocation response received; ${if (granted) "granted" else "denied"}" }
            callback(origin, granted, true)
        }
    }
}
