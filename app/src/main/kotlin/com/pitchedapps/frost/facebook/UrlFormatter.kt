package com.pitchedapps.frost.facebook

/**
 * Created by Allan Wang on 2017-07-07.
 */
class UrlFormatter(url: String) {
    val queries = mutableMapOf<String, String>()
    val cleaned: String

    init {
        var cleanedUrl = url
        discardable.forEach { cleanedUrl = cleanedUrlit, "") }
    }

    companion object {
        /**
         * Items here are explicitly removed from the url
         * Taken from FaceSlim
         * https://github.com/indywidualny/FaceSlim/blob/master/app/src/main/java/org/indywidualni/fblite/util/Miscellany.java
         */
        val discardable = arrayOf(
                "http://lm.facebook.com/l.php?u=",
                "https://lm.facebook.com/l.php?u=",
                "http://m.facebook.com/l.php?u=",
                "https://m.facebook.com/l.php?u=",
                "http://touch.facebook.com/l.php?u=",
                "https://touch.facebook.com/l.php?u=",
                "&h=.*",
                "\\?acontext=.*"
        )

        val decoder = mapOf(
                "%3C" to "<", "%3E" to ">", "%23" to "#", "%25" to "%",
                "%7B" to "{", "%7D" to "}", "%7C" to "|", "%5C" to "\\",
                "%5E" to "^", "%7E" to "~", "%5B" to "[", "%5D" to "]",
                "%60" to "`", "%3B" to ";", "%2F" to "/", "%3F" to "?",
                "%3A" to ":", "%40" to "@", "%3D" to "=", "%26" to "&",
                "%24" to "$", "%2B" to "+", "%22" to "\"", "%2C" to ",",
                "%20" to " "
        )
    }
}