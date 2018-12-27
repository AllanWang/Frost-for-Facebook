/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost

import com.pitchedapps.frost.facebook.requests.zip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.concurrent.Executors
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
        val data: LongArray = (0..15).map { Math.random() + base }
            .toTypedArray().zip(List<Long>::toLongArray) {
                Thread.sleep((it * 1000).toLong())
                System.currentTimeMillis() - now
            }.blockingGet()
        println(data.contentToString())
        assertTrue(
            data.all { it >= base * 1000 && it < base * 1000 * 5 },
            "zip did not seem to work on different threads"
        )
    }

@Test
@UseExperimental(ExperimentalCoroutinesApi::class)
fun channel() {
    val c = BroadcastChannel<Int>(100)
    runBlocking {
        launch(Dispatchers.IO) {
            println("1 start ${Thread.currentThread()}")
            for (i in c.openSubscription()) {
                println("1 $i")
            }
            println("1 end ${Thread.currentThread()}")
        }
        launch(Dispatchers.IO) {
            println("2 start ${Thread.currentThread()}")
            for (i in c.openSubscription()) {
                println("2 $i")
            }
            println("2 end ${Thread.currentThread()}")
        }
        c.send(1)
        c.send(2)
        c.send(3)
        delay(1000)
        c.close()
    }
}
}
