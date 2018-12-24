package com.pitchedapps.frost.facebook.requests

import com.fasterxml.jackson.databind.ObjectMapper
import com.pitchedapps.frost.internal.AUTH
import com.pitchedapps.frost.internal.COOKIE
import com.pitchedapps.frost.internal.USER_ID
import com.pitchedapps.frost.internal.authDependent
import okhttp3.Call
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

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
     * Used to emulate [executeForNoError]
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
        val auth = COOKIE.getAuth()
        assertNotNull(auth)
        assertEquals(USER_ID, auth.userId)
        assertEquals(COOKIE, auth.cookie)
        println("Test auth: ${auth.fb_dtsg}")
    }

    @Test
    fun markNotification() {
        val notifId = 1514443903880
        AUTH.markNotificationRead(notifId).call.assertNoError()
    }

    @Test
    fun fullSizeImage() {
        val fbid = 10155966932992838L // google's current cover photo
        val url = AUTH.getFullSizedImage(fbid).invoke()
        println(url)
        assertEquals(url?.startsWith("https://scontent"), true)
    }

    @Test
    fun testMenu() {
        val data = AUTH.getMenuData().invoke()
        assertNotNull(data)
        println(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data))
        assertTrue(data!!.data.isNotEmpty())
        assertTrue(data.footer.hasContent, "Footer may be badly parsed")
        val items = data.flatMapValid()
        assertTrue(items.size > 15, "Something may be badly parsed")
    }
}
