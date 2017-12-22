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
        assertFalse("#test-id".isIndependent, "id")
        assertFalse("#".isIndependent, "#")
        assertFalse("#!".isIndependent, "#!")
        assertFalse("#!/".isIndependent, "#!/")
        assertTrue("/this/is/valid".isIndependent, "url segments")
        assertTrue("#!/facebook/segment".isIndependent, "facebook segments")
    }

    @Test
    fun isFacebook() {
        assertFalse(GOOGLE.isFacebookUrl, "google")
        assertTrue(FACEBOOK_COM.isFacebookUrl, "facebook")
    }
}