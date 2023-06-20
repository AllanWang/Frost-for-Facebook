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
package com.pitchedapps.frost.webview

import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.web.state.FrostWebStore
import com.pitchedapps.frost.web.state.TabAction
import com.pitchedapps.frost.web.state.TabAction.ContentAction.UpdateProgressAction
import com.pitchedapps.frost.web.state.TabAction.ContentAction.UpdateTitleAction
import com.pitchedapps.frost.web.state.get

/** The default chrome client */
class FrostChromeClient(private val tabId: WebTargetId, private val store: FrostWebStore) :
  WebChromeClient() {

  private fun FrostWebStore.dispatch(action: TabAction.Action) {
    dispatch(TabAction(tabId = tabId, action = action))
  }

  override fun getDefaultVideoPoster(): Bitmap? =
    super.getDefaultVideoPoster() ?: Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)

  override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
    logger
      .atInfo()
      .log("Chrome Console %d: %s", consoleMessage.lineNumber(), consoleMessage.message())
    return true
  }

  override fun onReceivedTitle(view: WebView, title: String) {
    super.onReceivedTitle(view, title)
    if (title.startsWith("http")) return
    store.dispatch(UpdateTitleAction(title))
  }

  override fun onProgressChanged(view: WebView, newProgress: Int) {
    super.onProgressChanged(view, newProgress)
    // TODO remove?
    if (store.state[tabId]?.content?.progress == 100) return
    store.dispatch(UpdateProgressAction(newProgress))
  }

  //  override fun onShowFileChooser(
  //    webView: WebView,
  //    filePathCallback: ValueCallback<Array<Uri>?>,
  //    fileChooserParams: FileChooserParams
  //  ): Boolean {
  //    callbacks.openMediaPicker(filePathCallback, fileChooserParams)
  //    return true
  //  }

  //  override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean
  // {
  //    callbacks.onJsAlert(url, message, result)
  //    return true
  //  }
  //
  //  override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult):
  // Boolean {
  //    callbacks.onJsConfirm(url, message, result)
  //    return true
  //  }
  //
  //  override fun onJsBeforeUnload(
  //    view: WebView,
  //    url: String,
  //    message: String,
  //    result: JsResult
  //  ): Boolean {
  //    callbacks.onJsBeforeUnload(url, message, result)
  //    return true
  //  }
  //
  //  override fun onJsPrompt(
  //    view: WebView,
  //    url: String,
  //    message: String,
  //    defaultValue: String?,
  //    result: JsPromptResult
  //  ): Boolean {
  //    callbacks.onJsPrompt(url, message, defaultValue, result)
  //    return true
  //  }

  //  override fun onGeolocationPermissionsShowPrompt(
  //    origin: String,
  //    callback: GeolocationPermissions.Callback
  //  ) {
  //    L.i { "Requesting geolocation" }
  //    context.kauRequestPermissions(PERMISSION_ACCESS_FINE_LOCATION) { granted, _ ->
  //      L.i { "Geolocation response received; ${if (granted) "granted" else "denied"}" }
  //      callback(origin, granted, true)
  //    }
  //  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
