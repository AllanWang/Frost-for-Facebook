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
package com.pitchedapps.frost.facebook

import android.net.Uri
import com.pitchedapps.frost.utils.L
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Created by Allan Wang on 2017-07-07.
 *
 * Custom url builder so we can easily test it without the Android framework
 */
inline val String.formattedFbUrl: String
    get() = FbUrlFormatter(this).toString()

inline val Uri.formattedFbUri: Uri
    get() {
        val url = toString()
        return if (url.startsWith("http")) Uri.parse(url.formattedFbUrl) else this
    }

class FbUrlFormatter(url: String) {
    private val queries = mutableMapOf<String, String>()
    private val cleaned: String

    /**
     * Formats all facebook urls
     *
     * The order is very important:
     * 1. Wrapper links (discardables) are stripped away, resulting in the actual link
     * 2. CSS encoding is converted to normal encoding
     * 3. Url is completely decoded
     * 4. Url is split into sections
     */
    init {
        cleaned = clean(url)
    }

    fun clean(url: String): String {
        if (url.isBlank()) return ""
        var cleanedUrl = url
        if (cleanedUrl.startsWith("#!")) cleanedUrl = cleanedUrl.substring(2)
        val urlRef = cleanedUrl
        discardable.forEach { cleanedUrl = cleanedUrl.replace(it, "", true) }
        val changed = cleanedUrl != urlRef
        converter.forEach { (k, v) -> cleanedUrl = cleanedUrl.replace(k, v, true) }
        try {
            cleanedUrl = URLDecoder.decode(cleanedUrl, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            L.e(e) { "Failed url formatting" }
            return url
        }
        cleanedUrl = cleanedUrl.replace("&amp;", "&")
        if (changed && !cleanedUrl.contains("?")) //ensure we aren't missing '?'
            cleanedUrl = cleanedUrl.replaceFirst("&", "?")
        val qm = cleanedUrl.indexOf("?")
        if (qm > -1) {
            cleanedUrl.substring(qm + 1).split("&").forEach {
                val p = it.split("=")
                queries[p[0]] = p.elementAtOrNull(1) ?: ""
            }
            cleanedUrl = cleanedUrl.substring(0, qm)
        }
        discardableQueries.forEach { queries.remove(it) }
        if (cleanedUrl.startsWith("/")) cleanedUrl = FB_URL_BASE + cleanedUrl.substring(1)
        cleanedUrl = cleanedUrl.replaceFirst(
            ".facebook.com//",
            ".facebook.com/"
        ) //sometimes we are given a bad url
        L.v { "Formatted url from $url to $cleanedUrl" }
        return cleanedUrl
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(cleaned)
        if (queries.isNotEmpty()) {
            builder.append("?")
            queries.forEach { (k, v) ->
                if (v.isEmpty()) {
                    builder.append("$k&")
                } else {
                    builder.append("$k=$v&")
                }
            }
        }
        return builder.removeSuffix("&").toString()
    }

    fun toLogList(): List<String> {
        val list = mutableListOf(cleaned)
        queries.forEach { (k, v) -> list.add("\n- $k\t=\t$v") }
        list.add("\n\n${toString()}")
        return list
    }

    companion object {

        const val VIDEO_REDIRECT = "/video_redirect/?src="
        /**
         * Items here are explicitly removed from the url
         * Taken from FaceSlim
         * https://github.com/indywidualny/FaceSlim/blob/master/app/src/main/java/org/indywidualni/fblite/util/Miscellany.java
         *
         * Note: Typically, in this case, the redirect url should have all the necessary queries
         * I am unsure how Facebook reacts in all cases, so the ones after the redirect are appended on afterwards
         * That shouldn't break anything
         */
        val discardable = arrayOf(
            "http://lm.facebook.com/l.php?u=",
            "https://lm.facebook.com/l.php?u=",
            "http://m.facebook.com/l.php?u=",
            "https://m.facebook.com/l.php?u=",
            "http://touch.facebook.com/l.php?u=",
            "https://touch.facebook.com/l.php?u=",
            VIDEO_REDIRECT
        )

        /**
         * Queries that are not necessary for independent links
         *
         * acontext is not required for "friends interested in" notifications
         */
        val discardableQueries = arrayOf(
            "ref",
            "refid",
            "SharedWith",
            "fbclid",
            "_ft_",
            "_tn_",
            "_xt_",
            "bacr",
            "frefs",
            "hc_ref",
            "loc_ref",
            "pn_ref"
        )

        val converter = listOf(
            "\\3C " to "%3C", "\\3E " to "%3E", "\\23 " to "%23", "\\25 " to "%25",
            "\\7B " to "%7B", "\\7D " to "%7D", "\\7C " to "%7C", "\\5C " to "%5C",
            "\\5E " to "%5E", "\\7E " to "%7E", "\\5B " to "%5B", "\\5D " to "%5D",
            "\\60 " to "%60", "\\3B " to "%3B", "\\2F " to "%2F", "\\3F " to "%3F",
            "\\3A " to "%3A", "\\40 " to "%40", "\\3D " to "%3D", "\\26 " to "%26",
            "\\24 " to "%24", "\\2B " to "%2B", "\\22 " to "%22", "\\2C " to "%2C",
            "\\20 " to "%20"
        )
    }
}
