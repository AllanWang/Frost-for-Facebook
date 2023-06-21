/*
 * Copyright 2020 Allan Wang
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

import android.webkit.WebView
import com.pitchedapps.frost.webview.injection.JsBuilder
import com.pitchedapps.frost.webview.injection.JsInjector

/** Small misc inline css assets */
enum class CssActions(private val content: String) : JsInjector {
  FullSizeImage(
    "div._4prr[style*=\"max-width\"][style*=\"max-height\"]{max-width:none !important;max-height:none !important}",
  );

  private val injector: JsInjector =
    JsBuilder().css(content).single("css-small-assets-$name").build()

  override fun inject(webView: WebView) {
    injector.inject(webView)
  }
}
