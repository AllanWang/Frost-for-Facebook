package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.web.FrostWebViewClient
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.SingleSubject
import org.apache.commons.text.StringEscapeUtils
import java.util.*

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
        this.tag = tag
        return this
    }

    fun build() = JsInjector(toString())

    override fun toString(): String {
        val builder = StringBuilder().append("!function(){")
        if (css.isNotBlank()) {
            val cssMin = css.replace(Regex("\\s*\n\\s*"), "")
            builder.append("var a=document.createElement('style');a.innerHTML='$cssMin';document.head.appendChild(a);")
        }
        if (js.isNotBlank())
            builder.append(js)
        var content = builder.append("}()").toString()
        if (tag != null) content = singleInjector(tag!!, content)
        return content
    }

    private fun singleInjector(tag: String, content: String) = StringBuilder().apply {
        val name = "_frost_${tag.toLowerCase(Locale.CANADA)}"
        append("if (!window.hasOwnProperty(\"$name\")) {")
        append("console.log(\"Registering $name\");")
        append("window.$name = true;")
        append(content)
        append("}")
    }.toString()
}

/**
 * Contract for all injectors to allow it to interact properly with a webview
 */
interface InjectorContract {
    fun inject(webView: WebView) = inject(webView, {})
    fun inject(webView: WebView, callback: () -> Unit)
    /**
     * Toggle the injector (usually through Prefs
     * If false, will fallback to an empty action
     */
    fun maybe(enable: Boolean): InjectorContract = if (enable) this else JsActions.EMPTY
}

/**
 * Helper method to inject multiple functions simultaneously with a single callback
 */
fun WebView.jsInject(vararg injectors: InjectorContract, callback: ((Int) -> Unit) = {}) {
    val validInjectors = injectors.filter { it != JsActions.EMPTY }
    if (validInjectors.isEmpty()) return callback(0)
    val observables = Array(validInjectors.size, { SingleSubject.create<Unit>() })
    L.d { "Injecting ${observables.size} items" }
    Single.zip<Unit, Int>(observables.asList(), { it.size })
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { res, _ ->
                callback(res)
            }
    (0 until validInjectors.size).forEach { i -> validInjectors[i].inject(this, { observables[i].onSuccess(Unit) }) }
}

fun FrostWebViewClient.jsInject(vararg injectors: InjectorContract,
                                callback: ((Int) -> Unit) = {})
        = web.jsInject(*injectors, callback = callback)

/**
 * Wrapper class to convert a function into an injector
 */
class JsInjector(val function: String) : InjectorContract {
    override fun inject(webView: WebView, callback: () -> Unit) {
        webView.evaluateJavascript(function, { callback() })
    }
}