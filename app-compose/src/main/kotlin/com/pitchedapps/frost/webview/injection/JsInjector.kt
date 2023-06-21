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

import android.webkit.WebView
import org.apache.commons.text.StringEscapeUtils

interface JsInjector {

  fun inject(webView: WebView)

  companion object {

    val EMPTY: JsInjector = EmptyJsInjector

    operator fun invoke(content: String): JsInjector =
      object : JsInjector {
        override fun inject(webView: WebView) {
          webView.evaluateJavascript(content, null)
        }
      }
  }
}

private object EmptyJsInjector : JsInjector {
  override fun inject(webView: WebView) {
    // Noop
  }
}

data class OneShotJsInjector(val tag: String, val injector: JsInjector) : JsInjector {
  override fun inject(webView: WebView) {
    // TODO
  }
}

class JsBuilder {
  private val css = StringBuilder()
  private val js = StringBuilder()

  private var tag: String? = null

  fun css(css: String): JsBuilder {
    this.css.append(StringEscapeUtils.escapeEcmaScript(css))
    return this
  }

  fun js(content: String): JsBuilder {
    this.js.append(content)
    return this
  }

  fun single(tag: String): JsBuilder {
    this.tag = tag // TODO TagObfuscator.obfuscateTag(tag)
    return this
  }

  fun build() = JsInjector(toString())

  override fun toString(): String {
    val tag = this.tag
    val builder =
      StringBuilder().apply {
        if (css.isNotBlank()) {
          val cssMin = css.replace(Regex("\\s*\n\\s*"), "")
          append("var a=document.createElement('style');")
          append("a.innerHTML='$cssMin';")
          if (tag != null) {
            append("a.id='$tag';")
          }
          append("document.head.appendChild(a);")
        }
        if (js.isNotBlank()) {
          append(js)
        }
      }
    var content = builder.toString()
    if (tag != null) {
      content = singleInjector(tag, content)
    }
    return wrapAnonymous(content)
  }

  private fun wrapAnonymous(body: String) = "(function(){$body})();"

  private fun singleInjector(tag: String, content: String) =
    """
      if (!window.hasOwnProperty("$tag")) {
        console.log("Registering $tag");
        window.$tag = true;
        $content
      }
    """
      .trimIndent()
}
