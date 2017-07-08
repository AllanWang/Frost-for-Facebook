package com.pitchedapps.frost.utils

import android.net.Uri
import com.pitchedapps.frost.BuildConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


/**
 * Created by Allan Wang on 2017-07-07.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class UrlTest {

    @Test
    fun testParse() {
        val uri = Uri.parse("https://touch.facebook.com/story.php?story_fbid=1555753131153873&id=525538540842009&refid=7&_ft_=qid.6440135786032091148%3Amf_story_key.3325631938086219467%3Atop_level_post_id.1555752904487229%3Apage_id.525538540842009&__tn__=%2As")
        log(uri)
    }

    fun log(uri: Uri) {
        with(uri) {
            println("Host $host")
            print("Queries: ")
            println(queryParameterNames.toString())
        }
    }
}