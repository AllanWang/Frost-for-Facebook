package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.utils.isImageUrl
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * Created by Allan Wang on 2017-07-07.
 */
class FbUrlTest {

    @Suppress("NOTHING_TO_INLINE")
    inline fun assertFbFormat(expected: String, url: String) {
        val fbUrl = FbUrlFormatter(url)
        assertEquals(expected, fbUrl.toString(), "FbUrl Mismatch:\n${fbUrl.toLogList().joinToString("\n\t")}")
    }

    @Test
    fun base() {
        val url = "${FB_URL_BASE}relative/?asdf=1234&hjkl=7890"
        assertFbFormat(url, url)
    }

    @Test
    fun relative() {
        val url = "/relative/?asdf=1234&hjkl=7890"
        assertFbFormat("$FB_URL_BASE${url.substring(1)}", url)
    }

    @Test
    fun discard() {
        val prefix = "$FB_URL_BASE?test=1234"
        val suffix = "&apple=notorange"
        assertFbFormat("$prefix$suffix", "$prefix&ref=hello$suffix")
    }

    /**
     * Unnecessary wraps should be removed & the query items should be bound properly (first & to ?)
     */
    @Test
    fun queryConversion() {
        val url = "https://m.facebook.com/l.php?u=https%3A%2F%2Fgoogle.ca&h=hi"
        val expected = "https://google.ca?h=hi"
        assertFbFormat(expected, url)
    }

    @Test
    fun doubleDash() {
        assertFbFormat("${FB_URL_BASE}relative", "$FB_URL_BASE/relative")
    }

    @Test
    fun video() {
        //note that the video numbers have been changed to maintain privacy
        val url = "/video_redirect/?src=https%3A%2F%2Fvideo-yyz1-1.xx.fbcdn.net%2Fv%2Ft42.1790-2%2F2349078999904_n.mp4%3Fefg%3DeyJ87J9%26oh%3Df5777784%26oe%3D56FD4&source=media_collage&id=1735049&refid=8&_ft_=qid.6484464%3Amf_story_key.-43172431214%3Atop_level_post_id.102773&__tn__=FEH-R"
        val expected = "https://video-yyz1-1.xx.fbcdn.net/v/t42.1790-2/2349078999904_n.mp4?efg=eyJ87J9&oh=f5777784&oe=56FD4&source=media_collage&id=1735049&_ft_=qid.6484464:mf_story_key.-43172431214:top_level_post_id.102773&__tn__=FEH-R"
        assertFbFormat(expected, url)
    }


    @Test
    fun image() {
        arrayOf(
                "https://scontent-yyz1-1.xx.fbcdn.net/v/t1.0-9/fr/cp0/e15/q65/229_546131_836546862_n.jpg?efg=e343J9&oh=d4245b1&oe=5453"
//                "/photo/view_full_size/?fbid=1523&ref_component=mbasic_photo_permalink&ref_page=%2Fwap%2Fphoto.php&refid=153&_ft_=...",
//                "#!/photo/view_full_size/?fbid=1523&ref_component=mbasic_photo_permalink&ref_page=%2Fwap%2Fphoto.php&refid=153&_ft_=..."
        ).forEach {
            assertTrue(it.isImageUrl, "Failed to match image for $it")
        }
    }

    @Test
    fun antiImageRegex() {
        arrayOf(
                "http...fbcdn.net...mp4",
                "/photo/...png",
                "https://www.google.ca"
        ).forEach {
            assertFalse(it.isImageUrl, "Should not have matched image for $it")
        }

    }

    @Test
    fun viewFullImage() {
        val url = "https://scontent-yyz1-1.xx.fbcdn.net/v/t1.0-9/fr/cp0/e15/q65/asdf_n.jpg?efg=asdf&oh=asdf&oe=asdf"
        assertFbFormat(url, "#!$url")
    }

}