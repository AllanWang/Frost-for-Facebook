package com.pitchedapps.frost.utils

import com.pitchedapps.frost.facebook.FbUrlFormatter
import org.junit.Test


/**
 * Created by Allan Wang on 2017-07-07.
 */
class UrlTest {

    @Test
    fun testParse() {
        val url = FbUrlFormatter(TEST_URL)
        url.log()
        println(url.toString())
    }

    private fun FbUrlFormatter.log() = toLogList().forEach { println(it) }
}

const val TEST_URL = "https://touch.facebook.com/ScienceOrientationWeek/?refid=52&_ft_=qid.6440135786032091148%3Amf_story_key.3325631938086219467%3Atop_level_post_id.1555752904487229%3Apage_id.525538540842009&__tn__=C"