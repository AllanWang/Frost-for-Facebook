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
