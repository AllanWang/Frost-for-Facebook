package com.pitchedapps.frost.utils

import android.content.Context
import android.text.TextUtils
import ca.allanwang.kau.utils.use
import okhttp3.HttpUrl
import org.jetbrains.anko.doAsync

/**
 * Created by Allan Wang on 2017-09-24.
 */
object FrostAdBlock : AdBlocker("adblock.txt")

object FrostPglAdBlock : AdBlocker("pgl.yoyo.org.txt")

/**
 * Base implementation of an AdBlocker
 * Wrap this in a singleton and initialize it to use it
 */
open class AdBlocker(val assetPath: String) {

    val data: MutableSet<String> = mutableSetOf()

    fun init(context: Context) {
        doAsync {
            val content = context.assets.open(assetPath).bufferedReader().use { it.readLines().filter { !it.startsWith("#") } }
            data.addAll(content)
            L.i { "Initialized adblock for $assetPath with ${data.size} hosts" }
        }
    }

    fun isAd(url: String?): Boolean {
        url ?: return false
        val httpUrl = HttpUrl.parse(url) ?: return false
        return isAdHost(httpUrl.host())
    }

    tailrec fun isAdHost(host: String): Boolean {
        if (TextUtils.isEmpty(host))
            return false
        val index = host.indexOf(".")
        if (index < 0 || index + 1 < host.length) return false
        if (host.contains(host)) return true
        return isAdHost(host.substring(index + 1))
    }
}