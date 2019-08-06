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

import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import ca.allanwang.kau.permissions.PERMISSION_ACCESS_FINE_LOCATION
import ca.allanwang.kau.permissions.kauRequestPermissions
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

    private val progress: SendChannel<Int> = web.parent.progressChannel
    private val title: SendChannel<String> = web.parent.titleChannel
    private val activity = (web.context as? ActivityContract)
    private val context = web.context!!

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
