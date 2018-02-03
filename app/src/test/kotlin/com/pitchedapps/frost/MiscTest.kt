package com.pitchedapps.frost

import com.pitchedapps.frost.facebook.requests.zip
import com.pitchedapps.frost.injectors.CssHider
import org.junit.Test
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 2017-06-14.
 */
class MiscTest {

    /**
     * Spin off 15 threads
     * Pause each for 1 - 2s
     * Ensure that total zipped process does not take over 5s
     */
    @Test
    fun zip() {
        val now = System.currentTimeMillis()
        val base = 1
        val data = (0..15).map { Math.random() + base }.toTypedArray().zip(
                List<Long>::toLongArray,
                { Thread.sleep((it * 1000).toLong()); System.currentTimeMillis() - now }
        ).blockingGet()
        println(data.contentToString())
        assertTrue(data.all { it >= base * 1000 && it < base * 1000 * 5 },
                "zip did not seem to work on different threads")
    }
}