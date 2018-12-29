package com.pitchedapps.frost.rx

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.Timeout
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
        flyweight = Flyweight(GlobalScope, 100, 200L) {
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
        assertEquals(2, runBlocking { flyweight.fetch(1) }, "Invalid result")
        assertEquals(1, callCount.get(), "1 call expected")
    }

    @Test
    fun multipleWithOneKey() {
        val results: List<Int> = runBlocking {
            (0..1000).map {
                flyweight.scope.async {
                    flyweight.fetch(1)
                }
            }.map { it.await() }
        }
        assertEquals(1, callCount.get(), "1 call expected")
        assertEquals(1001, results.size, "Incorrect number of results returned")
        assertTrue(results.all { it == 2 }, "Result should all be 2")
    }

    @Test
    fun consecutiveReuse() {
        runBlocking {
            flyweight.fetch(1)
            assertEquals(1, callCount.get(), "1 call expected")
            flyweight.fetch(1)
            assertEquals(1, callCount.get(), "Reuse expected")
            Thread.sleep(300)
            flyweight.fetch(1)
            assertEquals(2, callCount.get(), "Refetch expected")
        }
    }

    @Test
    fun invalidate() {
        runBlocking {
            flyweight.fetch(1)
            assertEquals(1, callCount.get(), "1 call expected")
            flyweight.invalidate(1)
            flyweight.fetch(1)
            assertEquals(2, callCount.get(), "New call expected")
        }
    }

    @Test
    fun destroy() {
        runBlocking {
            val longRunningResult = async { flyweight.fetch(LONG_RUNNING_KEY) }
            flyweight.fetch(1)
            flyweight.cancel()
            try {
                flyweight.fetch(1)
                fail("Flyweight should not be fulfilled after it is destroyed")
            } catch (e: Exception) {
                assertEquals("Flyweight is not active", e.message, "Incorrect error found on fetch after destruction")
            }
            try {
                longRunningResult.await()
                fail("Flyweight should have cancelled previously running requests")
            } catch (e: Exception) {
                assertEquals(
                    "Flyweight cancelled",
                    e.message,
                    "Incorrect error found on fetch cancelled by destruction"
                )
            }
            println("Done")
        }
    }
}