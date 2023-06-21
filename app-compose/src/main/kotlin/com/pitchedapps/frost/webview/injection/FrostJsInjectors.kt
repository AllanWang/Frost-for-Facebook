/*
 * Copyright 2023 Allan Wang
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
package com.pitchedapps.frost.webview.injection

import android.content.Context
import android.webkit.WebView
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.webview.injection.assets.JsActions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class FrostJsInjectors
@Inject
internal constructor(
  @ApplicationContext private val context: Context,
) {

  @Volatile private var theme: JsInjector = JsInjector.EMPTY

  fun injectOnPageCommitVisible(view: WebView, url: String?) {
    logger.atInfo().log("inject page commit visible %b", theme != JsInjector.EMPTY)
    theme.inject(view)
  }

  private fun getTheme(): JsInjector {
    return try {
      val content =
        context.assets
          .open("frost/css/facebook/themes/material_glass.css")
          .bufferedReader()
          .use(BufferedReader::readText)
      JsBuilder().css(content).build()
    } catch (e: FileNotFoundException) {
      logger.atSevere().withCause(e).log("CssAssets file not found")
      JsActions.EMPTY
    }
  }

  suspend fun load() {
    withContext(Dispatchers.IO) { theme = getTheme() }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
