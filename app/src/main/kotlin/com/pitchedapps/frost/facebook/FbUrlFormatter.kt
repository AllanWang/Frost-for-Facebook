package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-07-07.
 *
 * Custom url builder so we can easily test it without the Android framework
 */
val String.formattedFbUrl: String
    get() = FbUrlFormatter(this).toString()

class FbUrlFormatter(url: String, decode: Boolean = false) {
    private val queries = mutableMapOf<String, String>()
    private val cleaned: String

    init {
        if (url.isBlank()) cleaned = ""
        else {
            var cleanedUrl = url
            discardable.forEach { cleanedUrl = cleanedUrl.replace(it, "", true) }
            val changed = cleanedUrl != url //note that discardables strip away the first '?'
            converter.forEach { (k, v) -> cleanedUrl = cleanedUrl.replace(k, v, true) }
            if (decode)
                decoder.forEach { (k, v) -> cleanedUrl = cleanedUrl.replace(k, v, true) }
            val qm = cleanedUrl.indexOf(if (changed) "&" else "?")
            if (qm > -1) {
                cleanedUrl.substring(qm + 1).split("&").forEach {
                    val p = it.split("=")
                    queries.put(p[0], p.elementAtOrNull(1) ?: "")
                }
                cleanedUrl = cleanedUrl.substring(0, qm)
            }
            discardableQueries.forEach { queries.remove(it) }
            if (cleanedUrl.startsWith("#!")) cleanedUrl = cleanedUrl.substring(2)
            if (cleanedUrl.startsWith("/")) cleanedUrl = FB_URL_BASE + cleanedUrl.substring(1)
            cleanedUrl = cleanedUrl.replaceFirst(".facebook.com//", ".facebook.com/") //sometimes we are given a bad url
            L.v(null, "Formatted url from $url to $cleanedUrl")
            cleaned = cleanedUrl
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(cleaned)
        if (queries.isNotEmpty()) {
            builder.append("?")
            queries.forEach { (k, v) -> builder.append("$k=$v&") }
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
        /**
         * Items here are explicitly removed from the url
         * Taken from FaceSlim
         * https://github.com/indywidualny/FaceSlim/blob/master/app/src/main/java/org/indywidualni/fblite/util/Miscellany.java
         */
        @JvmStatic
        val discardable = arrayOf(
                "http://lm.facebook.com/l.php?u=",
                "https://lm.facebook.com/l.php?u=",
                "http://m.facebook.com/l.php?u=",
                "https://m.facebook.com/l.php?u=",
                "http://touch.facebook.com/l.php?u=",
                "https://touch.facebook.com/l.php?u=",
                "/video_redirect/?src="
        )

        @JvmStatic
        val discardableQueries = arrayOf("ref", "refid")

        @JvmStatic
        val decoder = mapOf(
                "%3C" to "<", "%3E" to ">", "%23" to "#", "%25" to "%",
                "%7B" to "{", "%7D" to "}", "%7C" to "|", "%5C" to "\\",
                "%5E" to "^", "%7E" to "~", "%5B" to "[", "%5D" to "]",
                "%60" to "`", "%3B" to ";", "%2F" to "/", "%3F" to "?",
                "%3A" to ":", "%40" to "@", "%3D" to "=", "%26" to "&",
                "%24" to "$", "%2B" to "+", "%22" to "\"", "%2C" to ",",
                "%20" to " "
        )

        @JvmStatic
        val converter = mapOf(
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