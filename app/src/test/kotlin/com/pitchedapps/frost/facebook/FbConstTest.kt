package com.pitchedapps.frost.facebook

import kotlin.test.Test
import kotlin.test.assertFalse

class FbConstTest {

    private val constants = listOf(
        FACEBOOK_COM,
        MESSENGER_COM,
        FBCDN_NET,
        WWW_FACEBOOK_COM,
        WWW_MESSENGER_COM,
        HTTPS_FACEBOOK_COM,
        HTTPS_MESSENGER_COM,
        FACEBOOK_BASE_COM,
        FB_URL_BASE,
        FACEBOOK_MBASIC_COM,
        FB_URL_MBASIC_BASE,
        FB_LOGIN_URL,
        FB_HOME_URL,
        MESSENGER_THREAD_PREFIX
    )

    /**
     * Make sure we don't have accidental double forward slashes after appending
     */
    @Test
    fun doubleForwardSlashTest() {
        constants.forEach {
            assertFalse(
                it.replace("https://", "").contains("//"),
                "Accidental forward slash for $it"
            )
        }
    }
}