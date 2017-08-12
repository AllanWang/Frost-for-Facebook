package com.pitchedapps.frost.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Element
import org.jsoup.safety.Whitelist

/**
 * Created by Allan Wang on 2017-08-10.
 *
 * Parses html with Jsoup and cleans the data, emitting just the frame containing debugging info
 *
 * Removes text, removes unnecessary nodes
 */
fun String.cleanHtml() = cleanText().cleanJsoup()

internal fun String.cleanText(): String = replace(Regex(">.+?<"), "><")

internal fun String.cleanJsoup(): String = Jsoup.clean(this, PrivacyWhitelist())

class PrivacyWhitelist : Whitelist() {

    val blacklistAttrs = arrayOf("style", "aria-label", "rel")
    val blacklistTags = arrayOf("body", "html", "head", "i", "b", "u", "style", "br", "p", "span", "ul", "ol", "li")

    override fun isSafeAttribute(tagName: String, el: Element, attr: Attribute): Boolean {
        val key = attr.key
        if (key == "href") attr.setValue("-")
        return key !in blacklistAttrs
    }

    override fun isSafeTag(tag: String) = tag !in blacklistTags
}
