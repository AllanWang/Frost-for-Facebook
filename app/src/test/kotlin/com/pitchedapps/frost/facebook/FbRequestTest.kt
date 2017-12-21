package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.internal.COOKIE
import com.pitchedapps.frost.internal.FB_DTSG
import com.pitchedapps.frost.internal.USER_ID
import org.junit.Assume
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Created by Allan Wang on 21/12/17.
 */
class FbRequestTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun before() {
            Assume.assumeTrue(COOKIE.isNotEmpty())
        }

        val AUTH: RequestAuth by lazy { RequestAuth(USER_ID, COOKIE, FB_DTSG) }
    }

    @Test
    fun userIdRegex() {
        val id = 12349876L
        val cookie = "wd=1366x615; c_user=$id; act=1234%2F12; m_pixel_ratio=1; presence=hello; x-referer=asdfasdf"
        assertEquals(id, FB_USER_MATCHER.find(cookie)?.groupValues?.get(1)?.toLong())
    }

    @Test
    fun fbDtsgRegex() {
        val fb_dtsg = "readme"
        val input = "data-sigil=\"mbasic_inline_feed_composer\">\u003Cinput type=\"hidden\" name=\"fb_dtsg\" value=\"$fb_dtsg\" autocomplete=\"off\" \\/>\u003Cinput type=\"hidden\" name=\"privacyx\" value=\"12345\""
        assertEquals(fb_dtsg, FB_DTSG_MATCHER.find(input)?.groupValues?.get(1))
    }

    @Test
    fun auth() {
        val auth = (USER_ID to COOKIE).getAuth()
        assertNotNull(auth)
        assertEquals(USER_ID, auth!!.userId)
        assertEquals(COOKIE, auth.cookie)
        println("Test auth: priv $FB_DTSG, test ${auth.fb_dtsg}")
    }

    @Test
    fun markNotification() {
        val notifId = 1513544657695779

        val out = AUTH.markNotificationRead(notifId)
                .execute().body()?.string() ?: ""
        println(out)

        assertFalse(out.contains("error"))
    }

}