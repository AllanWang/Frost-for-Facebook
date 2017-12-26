package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.internal.AUTH
import com.pitchedapps.frost.internal.COOKIE
import com.pitchedapps.frost.internal.USER_ID
import com.pitchedapps.frost.internal.authDependent
import okhttp3.Call
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.*

/**
 * Created by Allan Wang on 21/12/17.
 */
class FbRequestTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun before() {
            authDependent()
        }
    }

    /**
     * Used to emulate [executeAndCheck]
     * Must be consistent with that method
     */
    private fun Call.assertNoError() {
        val data = execute().body()?.string() ?: fail("Content was null")
        println("Call response: $data")
        assertTrue(data.isNotEmpty(), "Content was empty")
        assertFalse(data.contains("error"), "Content had error")
    }

    @Test
    fun auth() {
        val auth = (USER_ID to COOKIE).getAuth()
        assertNotNull(auth)
        assertEquals(USER_ID, auth.userId)
        assertEquals(COOKIE, auth.cookie)
        println("Test auth: ${auth.fb_dtsg}")
    }

    @Test
    fun markNotification() {
        val notifId = 1513544657695779
        AUTH.markNotificationRead(notifId).assertNoError()
    }

}