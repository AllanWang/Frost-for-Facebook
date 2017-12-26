package com.pitchedapps.frost.parsers

import com.pitchedapps.frost.facebook.FB_EPOCH_MATCHER
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.facebook.get
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 2017-10-06.
 */
class MessageParserTest {

    @Test
    fun basic() = debug("messages", MessageParser)

    @Test
    fun parseEpoch() {
        val input = "{\"time\":1507301642,\"short\":true,\"forceseconds\":false}"
        assertEquals(1507301642, FB_EPOCH_MATCHER.find(input)[1]!!.toLong())
    }

    @Test
    fun parseImage() {
        var input = "https\\3a //scontent.fyhu1-1.fna.fbcdn.net/v/t1.0-1/cp0/e15/q65/p100x100/12994387_243040309382307_4586627375882013710_n.jpg?efg\\3d eyJpIjoidCJ9\\26 oh\\3d b9ae0d7a1298989fe24873e2ee4054b6\\26 oe\\3d 5A3A7FE1"
        input = input.formattedFbUrl
        println(input)
    }
}