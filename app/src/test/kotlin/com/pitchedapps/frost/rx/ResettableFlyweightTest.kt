package com.pitchedapps.frost.rx

import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Created by Allan Wang on 07/01/18.
 */
private inline val threadId
    get() = Thread.currentThread().id

class ResettableFlyweightTest {

    class IntFlyweight : RxFlyweight<Int, Long, Long>() {
        override fun call(input: Int): Long {
            println("Call for $input on thread $threadId")
            Thread.sleep(20)
            return System.currentTimeMillis()
        }

        override fun validate(input: Int, cond: Long) = System.currentTimeMillis() - cond < 500

        override fun cache(input: Int): Long = System.currentTimeMillis()
    }

    private lateinit var flyweight: IntFlyweight
    private lateinit var latch: CountDownLatch

    @Before
    fun init() {
        flyweight = IntFlyweight()
        latch = CountDownLatch(1)
    }

    @Test
    fun testCache() {
        flyweight(1).subscribe { i ->
            flyweight(1).subscribe { j ->
                assertEquals(i, j, "Did not use cache during calls")
                latch.countDown()
            }
        }
        latch.await()
    }

    @Test
    fun testNoCache() {
        flyweight(1).subscribe { i ->
            flyweight(2).subscribe { j ->
                assertNotEquals(i, j, "Should not use cache for calls with different keys")
                latch.countDown()
            }
        }
        latch.await()
    }


}