package com.pitchedapps.frost.injectors

import android.webkit.WebView

/**
 * Created by Allan Wang on 2017-05-31.
 */
class JsBuilder {
    private val builder = StringBuilder()

    init {
        builder.append("javascript:(function(){")
    }

    private fun getElementById(id: String) = "document.getElementById(\"$id\")"

    private fun hideElementById(id: String) {
        builder.append(getElementById(id)).append(".style.display=\"none\";")
    }

    fun hideElementById(vararg ids: String) {
        ids.forEach { hideElementById(it) }
    }

    fun build() = builder.toString() + "})()"

    fun inject(webView: WebView) {
        webView.loadUrl(build())
    }

    fun removeAllStyles() {

    }

    override fun toString() = build()
}