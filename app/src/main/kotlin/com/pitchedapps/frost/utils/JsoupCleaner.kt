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

internal fun String.cleanText(): String = replace(Regex(">(?s).+?<"), "><")

internal fun String.cleanJsoup(): String = Jsoup.clean(this, PrivacyWhitelist())

class PrivacyWhitelist : Whitelist() {

    val blacklistAttrs = arrayOf("style", "aria-label", "rel")
    val blacklistTags = arrayOf(
        "body", "html", "head", "i", "b", "u", "style", "script",
        "br", "p", "span", "ul", "ol", "li"
    )

    override fun isSafeAttribute(tagName: String, el: Element, attr: Attribute): Boolean {
        val key = attr.key
        if (key == "href") attr.setValue("-")
        return key !in blacklistAttrs
    }

    override fun isSafeTag(tag: String) = tag !in blacklistTags
}
