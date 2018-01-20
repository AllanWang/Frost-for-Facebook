package com.pitchedapps.frost.utils

import com.pitchedapps.frost.facebook.FACEBOOK_COM
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 2017-11-15.
 */
class UrlTests {

    val GOOGLE = "https://www.google.ca"

    @Test
    fun independence() {

        mapOf(
                GOOGLE to true,
                FACEBOOK_COM to true,
                "#!/photos/viewer/?photoset_token=pcb.1234" to false,
                "#test-id" to false,
                "#" to false,
                "#!" to false,
                "#!/" to false,
                "#!/events/permalink/going/?event_id=" to false,
                "/this/is/valid" to true,
                "#!/facebook/segment" to true
        ).forEach { (url, valid) ->
            assertEquals(valid, url.isIndependent,
                    "Independence test failed for $url; should be $valid")
        }
    }

    @Test
    fun isFacebook() {
        assertFalse(GOOGLE.isFacebookUrl, "google")
        assertTrue(FACEBOOK_COM.isFacebookUrl, "facebook")
    }
}