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
package com.pitchedapps.frost.webview.injection.assets

import android.content.Context
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.webview.injection.JsBuilder
import com.pitchedapps.frost.webview.injection.JsInjector
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Allan Wang on 2017-05-31. Mapping of the available assets The enum name must match the
 * js file name
 */
enum class JsAssets(private val singleLoad: Boolean = true) : JsInjector {
  CLICK_A,
  CONTEXT_A,
  MEDIA,
  HEADER_BADGES,
  TEXTAREA_LISTENER,
  NOTIF_MSG,
  DOCUMENT_WATCHER,
  HORIZONTAL_SCROLLING,
  AUTO_RESIZE_TEXTAREA(singleLoad = false),
  SCROLL_STOP,
  ;

  @VisibleForTesting internal val file = "${name.lowercase(Locale.CANADA)}.js"

  private fun injectorBlocking(context: Context): JsInjector {
    return try {
      val content =
        context.assets.open("frost/js/$file").bufferedReader().use(BufferedReader::readText)
      JsBuilder().js(content).run { if (singleLoad) single(name) else this }.build()
    } catch (e: FileNotFoundException) {
      logger.atWarning().withCause(e).log("JsAssets file not found")
      JsInjector.EMPTY
    }
  }

  private var injector: JsInjector = JsInjector.EMPTY

  override fun inject(webView: WebView) {
    injector.inject(webView)
  }

  private suspend fun load(context: Context) {
    withContext(Dispatchers.IO) { injector = injectorBlocking(context) }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()

    suspend fun load(context: Context) =
      withContext(Dispatchers.IO) { JsAssets.values().forEach { it.load(context) } }
  }
}

fun List<JsInjector>.inject(webView: WebView) {
  forEach { it.inject(webView) }
}
