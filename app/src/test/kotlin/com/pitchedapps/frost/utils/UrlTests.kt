package com.pitchedapps.frost.utils

import com.pitchedapps.frost.facebook.FACEBOOK_COM
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 2017-11-15.
 */
class UrlTests {

    val GOOGLE = "https://www.google.ca"

    @Test
    fun independence() {
        assertTrue(GOOGLE.isIndependent, "google")
        assertTrue(FACEBOOK_COM.isIndependent, "facebook")
        assertFalse("#!/photos/viewer/?photoset_token=pcb.1234".isIndependent, "photo")
    }

    @Test
    fun isFacebook() {
        assertFalse(GOOGLE.isFacebookUrl, "google")
        assertTrue(FACEBOOK_COM.isFacebookUrl, "facebook")
    }
}