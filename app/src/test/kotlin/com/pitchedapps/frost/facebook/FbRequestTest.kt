package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.internal.COOKIE
import com.pitchedapps.frost.internal.FB_DTSG
import com.pitchedapps.frost.internal.USER_ID
import org.junit.Assume
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertFalse

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
    fun markNotification() {
        val notifId = 1513544657695779

        val out = AUTH.markNotificationRead(notifId)
                .execute().body()?.string() ?: ""
        println(out)

        assertFalse(out.contains("error"))
    }

}