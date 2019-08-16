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
package com.pitchedapps.frost.injectors

import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.web.FrostWebViewClient
import org.apache.commons.text.StringEscapeUtils
import kotlin.random.Random

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
        this.tag = TagObfuscator.obfuscateTag(tag)
        return this
    }

    fun build() = JsInjector(toString())

    override fun toString(): String {
        val tag = this.tag
        val builder = StringBuilder().apply {
            append("!function(){")
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
        var content = builder.append("}()").toString()
        if (tag != null) {
            content = singleInjector(tag, content)
        }
        return content
    }

    private fun singleInjector(tag: String, content: String) = StringBuilder().apply {
        append("if (!window.hasOwnProperty(\"$tag\")) {")
        append("console.log(\"Registering $tag\");")
        append("window.$tag = true;")
        append(content)
        append("}")
    }.toString()
}

/**
 * Contract for all injectors to allow it to interact properly with a webview
 */
interface InjectorContract {
    fun inject(webView: WebView)
    /**
     * Toggle the injector (usually through Prefs
     * If false, will fallback to an empty action
     */
    fun maybe(enable: Boolean): InjectorContract = if (enable) this else JsActions.EMPTY
}

/**
 * Helper method to inject multiple functions simultaneously with a single callback
 */
fun WebView.jsInject(vararg injectors: InjectorContract) {
    injectors.filter { it != JsActions.EMPTY }.forEach {
        it.inject(this)
    }
}

fun FrostWebViewClient.jsInject(vararg injectors: InjectorContract) = web.jsInject(*injectors)

/**
 * Wrapper class to convert a function into an injector
 */
class JsInjector(val function: String) : InjectorContract {
    override fun inject(webView: WebView) =
        webView.evaluateJavascript(function, null)
}

/**
 * Helper object to obfuscate window tags for JS injection.
 */
@VisibleForTesting
internal object TagObfuscator {

    fun obfuscateTag(tag: String): String {
        val rnd = Random(tag.hashCode() + salt)
        val obfuscated = buildString {
            append(prefix)
            append('_')
            appendRandomChars(rnd, 16)
        }
        L.v { "TagObfuscator: Obfuscating tag '$tag' to '$obfuscated'" }
        return obfuscated
    }

    private val salt: Long = System.currentTimeMillis()

    private val prefix: String by lazy {
        val rnd = Random(System.currentTimeMillis())
        buildString { appendRandomChars(rnd, 8) }
    }

    private fun Appendable.appendRandomChars(random: Random, count: Int) {
        for (i in 1..count) {
            append('a' + random.nextInt(26))
        }
    }
}
