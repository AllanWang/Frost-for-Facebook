package com.pitchedapps.frost.facebook

import okhttp3.HttpUrl
import okio.Utf8
import org.junit.Test
import java.net.URLDecoder
import kotlin.test.assertEquals


/**
 * Created by Allan Wang on 2017-07-07.
 */
class FbUrlTest {

    fun assertFbFormat(expected: String, url: String) {
        val fbUrl = FbUrlFormatter(url)
        assertEquals(expected, fbUrl.toString(), "FbUrl Mismatch:\n${fbUrl.toLogList().joinToString("\n\t")}")
    }

    @Test
    fun base() {
        val url = "https://touch.facebook.com/relative/?asdf=1234&hjkl=7890"
        assertFbFormat(url, url)
    }

    @Test
    fun relative() {
        val url = "/relative/?asdf=1234&hjkl=7890"
        assertFbFormat("$FB_URL_BASE${url.substring(1)}", url)
    }

    @Test
    fun discard() {
        val prefix = "$FB_URL_BASE?test=1234"
        val suffix = "&apple=notorange"
        assertFbFormat("$prefix$suffix", "$prefix&ref=hello$suffix")
    }

    @Test
    fun doubleDash() {
        assertFbFormat("${FB_URL_BASE}relative", "$FB_URL_BASE/relative")
    }

}