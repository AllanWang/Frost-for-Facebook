package com.pitchedapps.frost.rx

import com.pitchedapps.frost.internal.concurrentTest
import org.junit.Before
import org.junit.Test

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

    @Before
    fun init() {
        flyweight = IntFlyweight()
    }

    @Test
    fun testCache() = concurrentTest { result ->
        flyweight(1).subscribe { i, _ ->
            flyweight(1).subscribe { j, _ ->
                if (i != null && i == j)
                    result.onComplete()
                else
                    result.onError("Did not use cache during calls")
            }
        }
    }

    @Test
    fun testNoCache() = concurrentTest { result ->
        flyweight(1).subscribe { i, _ ->
            flyweight(2).subscribe { j, _ ->
                if (i != null && i != j)
                    result.onComplete()
                else
                    result.onError("Should not use cache for calls with different keys")
            }
        }
    }


}