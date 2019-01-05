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
package com.pitchedapps.frost.kotlin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.Timeout
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class FlyweightTest {

    @get:Rule
    val globalTimeout: Timeout = Timeout.seconds(5)

    lateinit var flyweight: Flyweight<Int, Int>

    lateinit var callCount: AtomicInteger

    private val LONG_RUNNING_KEY = -78

    @BeforeTest
    fun before() {
        callCount = AtomicInteger(0)
        flyweight = Flyweight(GlobalScope, 200L) {
            callCount.incrementAndGet()
            when (it) {
                LONG_RUNNING_KEY -> Thread.sleep(100000)
                else -> Thread.sleep(100)
            }
            it * 2
        }
    }

    @Test
    fun basic() {
        assertEquals(2, runBlocking { flyweight.fetch(1).await() }, "Invalid result")
        assertEquals(1, callCount.get(), "1 call expected")
    }

    @Test
    fun multipleWithOneKey() {
        val results: List<Int> = runBlocking {
            (0..1000).map {
                flyweight.fetch(1)
            }.map { it.await() }
        }
        assertEquals(1, callCount.get(), "1 call expected")
        assertEquals(1001, results.size, "Incorrect number of results returned")
        assertTrue(results.all { it == 2 }, "Result should all be 2")
    }

    @Test
    fun consecutiveReuse() {
        runBlocking {
            flyweight.fetch(1).await()
            assertEquals(1, callCount.get(), "1 call expected")
            flyweight.fetch(1).await()
            assertEquals(1, callCount.get(), "Reuse expected")
            Thread.sleep(300)
            flyweight.fetch(1).await()
            assertEquals(2, callCount.get(), "Refetch expected")
        }
    }

    @Test
    fun invalidate() {
        runBlocking {
            flyweight.fetch(1).await()
            assertEquals(1, callCount.get(), "1 call expected")
            flyweight.invalidate(1)
            flyweight.fetch(1).await()
            assertEquals(2, callCount.get(), "New call expected")
        }
    }

    @Test
    fun destroy() {
        runBlocking {
            val longRunningResult = flyweight.fetch(LONG_RUNNING_KEY)
            flyweight.fetch(1).await()
            flyweight.cancel()
            try {
                flyweight.fetch(1).await()
                fail("Flyweight should not be fulfilled after it is destroyed")
            } catch (ignore: CancellationException) {
            }
            try {
                assertFalse(longRunningResult.isActive, "Long running result should no longer be active")
                longRunningResult.await()
                fail("Flyweight should have cancelled previously running requests")
            } catch (ignore: CancellationException) {
            }
        }
    }
}
