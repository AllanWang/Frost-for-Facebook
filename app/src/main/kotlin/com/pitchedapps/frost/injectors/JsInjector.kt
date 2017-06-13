package com.pitchedapps.frost.injectors

import android.webkit.WebView

class JsBuilder {
    private val css = StringBuilder()
    private val js = StringBuilder()

    fun css(css: String): JsBuilder {
        this.css.append(css)
        return this
    }

    fun js(content: String): JsBuilder {
        this.js.append(content)
        return this
    }

    fun build() = JsInjector(toString())

    override fun toString(): String {
        val builder = StringBuilder().append("!function(){")
        if (css.isNotBlank()) {
            val cssMin = css.replace(Regex("\\s+"), "")
            builder.append("var a=document.createElement('style');a.innerHTML='$cssMin';document.head.appendChild(a);")
        }
        if (js.isNotBlank())
            builder.append(js)
        return builder.append("}()").toString()
    }
}

class JsInjector(val function: String) {
    fun inject(webView: WebView, callback: ((String) -> Unit)? = null) {
        webView.evaluateJavascript(function, { value -> callback?.invoke(value) })
    }
}
