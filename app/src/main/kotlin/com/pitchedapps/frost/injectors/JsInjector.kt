package com.pitchedapps.frost.injectors

import android.webkit.WebView

/**
 * Created by Allan Wang on 2017-05-31.
 */
enum class JsActions(val function: String) {
    HIDE("style.display='none'"),
    REMOVE("remove()")
}

class VariableGenerator {

    var count = 0

    val next: String
        get() {
            var key = count
            count++
            if (key == 0) return "a"
            val name = StringBuilder()
            while (key > 0) {
                name.append(((key % 26) + 'a'.toInt()).toChar())
                key /= 26
            }
            return name.toString()
        }

    fun reset() {
        count = 0
    }
}

class JsBuilder {

    private val map: MutableMap<String, MutableSet<JsActions>> = mutableMapOf()
    private val v = VariableGenerator()
    private val css: StringBuilder by lazy { StringBuilder() }

    fun append(action: JsActions, vararg ids: String): JsBuilder {
        ids.forEach { id -> map[id]?.add(action) ?: map.put(id, mutableSetOf(action)) }
        return this
    }

    fun css(css: String): JsBuilder {
        this.css.append(css.trim())
        return this
    }

    fun build() = JsInjector(toString())

    override fun toString(): String {
        v.reset()
        val builder = StringBuilder().append("!function(){")
        map.forEach { id, actions ->
            if (actions.size == 1) {
                builder.append("document.getElementById('$id').${actions.first().function};")
            } else {
                val name = v.next
                builder.append("var $name=document.getElementById('$id');")
                actions.forEach { a -> builder.append("$name.${a.function};") }
            }
        }
        if (css.isNotBlank()) {
            val name = v.next
            val cssMin = css.replace(Regex("\\s+"), "")
            builder.append("var $name=document.createElement('style');$name.innerHTML='$cssMin';document.head.appendChild($name);")
        }
        return builder.append("}()").toString()
    }
}

class JsInjector(val function: String) {
    fun inject(webView: WebView, callback: ((String) -> Unit)? = null) {
        webView.evaluateJavascript(function, { value -> callback?.invoke(value) })
    }
}
