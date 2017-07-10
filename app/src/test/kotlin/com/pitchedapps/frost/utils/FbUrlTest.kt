package com.pitchedapps.frost.utils

import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbUrlFormatter
import org.junit.Test
import kotlin.test.assertEquals


/**
 * Created by Allan Wang on 2017-07-07.
 */
class FbUrlTest {

    @Test
    fun base() {
        val url = "https://touch.facebook.com/relative/?asdf=1234&hjkl=7890"
        assertFbFormat(url, url)
    }

    @Test
    fun relative() {
        val url = "/relative/?asdf=1234&hjkl=7890"
        assertFbFormat("$FB_URL_BASE$url", url)
    }

    @Test
    fun redirect() {
        val url = "/relative/?asdf=1234&hjkl=7890"
        assertFbFormat("$FB_URL_BASE$url", "https://touch.facebook.com/l.php?u=$url")
    }

    @Test fun discard() {
        val prefix = "$FB_URL_BASE/?test=1234"
        val suffix = "&apple=notorange"
        assertFbFormat("$prefix$suffix", "$prefix&ref=hello$suffix")
    }

    fun assertFbFormat(expected: String, url: String) {
        val fbUrl = FbUrlFormatter(url)
        assertEquals(expected, fbUrl.toString(), "FbUrl Mismatch:\n${fbUrl.toLogList().joinToString("\n\t")}")
    }
}