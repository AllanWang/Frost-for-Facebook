package com.pitchedapps.frost.facebook

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 24/12/17.
 */
class FbRegexTest {
    @Test
    fun userIdRegex() {
        val id = 12349876L
        val cookie = "wd=1366x615; c_user=$id; act=1234%2F12; m_pixel_ratio=1; presence=hello; x-referer=asdfasdf"
        assertEquals(id, FB_USER_MATCHER.find(cookie)[1]?.toLong())
    }

    @Test
    fun fbDtsgRegex() {
        val fb_dtsg = "readme"
        val input = "data-sigil=\"mbasic_inline_feed_composer\">\u003Cinput type=\"hidden\" name=\"fb_dtsg\" value=\"$fb_dtsg\" autocomplete=\"off\" \\/>\u003Cinput type=\"hidden\" name=\"privacyx\" value=\"12345\""
        assertEquals(fb_dtsg, FB_DTSG_MATCHER.find(input)[1])
    }
}