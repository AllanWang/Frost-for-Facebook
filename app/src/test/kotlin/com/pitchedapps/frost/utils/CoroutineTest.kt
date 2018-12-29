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
package com.pitchedapps.frost.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.count
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Collection of tests around coroutines
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
class CoroutineTest {

    /**
     * Hooks onto the refresh channel for one true -> false cycle.
     * Returns the list of event ids that were emitted
     */
    private suspend fun transition(channel: ReceiveChannel<Pair<Boolean, Int>>): List<Pair<Boolean, Int>> {
        var refreshed = false
        return listen(channel) { (refreshing, _) ->
            if (refreshed && !refreshing)
                return@listen true
            if (refreshing)
                refreshed = true
            return@listen false
        }
    }

    private suspend fun <T> listen(channel: ReceiveChannel<T>, shouldEnd: suspend (T) -> Boolean = { false }): List<T> =
        withContext(Dispatchers.IO) {
            val data = mutableListOf<T>()
            for (c in channel) {
                data.add(c)
                if (shouldEnd(c)) break
            }
            channel.cancel()
            return@withContext data
        }

    /**
     * When refreshing, we have a temporary subscriber that hooks onto a single cycle.
     * The refresh channel only contains booleans, but for the sake of identification,
     * each boolean will have a unique integer attached.
     *
     * Things to note:
     * Subscription should be opened outside of async, since we don't want to miss any events.
     */
    @Test
    fun refreshSubscriptions() {
        val refreshChannel = BroadcastChannel<Pair<Boolean, Int>>(100)
        runBlocking {
            // Listen to all events
            val fullReceiver = refreshChannel.openSubscription()
            val fullDeferred = async { listen(fullReceiver) }

            refreshChannel.send(true to 1)
            refreshChannel.send(false to 2)
            refreshChannel.send(true to 3)

            val partialReceiver = refreshChannel.openSubscription()
            val partialDeferred = async { transition(partialReceiver) }
            refreshChannel.send(false to 4)
            refreshChannel.send(true to 5)
            refreshChannel.send(false to 6)
            refreshChannel.send(true to 7)
            refreshChannel.close()
            val fullStream = fullDeferred.await()
            val partialStream = partialDeferred.await()

            assertEquals(
                7,
                fullStream.size,
                "Full stream should contain all events"
            )
            assertEquals(
                listOf(false to 4, true to 5, false to 6),
                partialStream,
                "Partial stream should include up until first true false pair"
            )
        }
    }

    /**
     * Sanity check to ensure that contexts are being honoured
     */
    @Test
    fun contextSwitching() {
        val mainTag = "main-test"
        val mainDispatcher = Executors.newSingleThreadExecutor { r ->
            Thread(r, mainTag)
        }.asCoroutineDispatcher()

        val channel = BroadcastChannel<String>(100)

        runBlocking(Dispatchers.IO) {
            val receiver1 = channel.openSubscription()
            val receiver2 = channel.openSubscription()
            launch(mainDispatcher) {
                for (thread in receiver1) {
                    assertTrue(
                        Thread.currentThread().name.startsWith(mainTag),
                        "Channel should be received in main thread"
                    )
                    assertFalse(
                        thread.startsWith(mainTag),
                        "Channel execution should not be in main thread"
                    )
                }
            }
            listOf(EmptyCoroutineContext, Dispatchers.IO, Dispatchers.Default, Dispatchers.IO).map {
                async(it) { channel.send(Thread.currentThread().name) }
            }.joinAll()
            channel.close()
            assertEquals(4, receiver2.count(), "Not all events received")
        }
    }

    /**
     * Not a true throttle, but for things like fetching header badges, we want to avoid simultaneous fetches.
     * As a result, I want to test that the usage of offer along with a rendezvous channel will work as I expect.
     * Events should be consumed when there is no pending consumer on previous elements.
     */
    @Test
    fun throttledChannel() {
        val channel = Channel<Int>(Channel.RENDEZVOUS)
        runBlocking {
            val deferred = async {
                listen(channel) {
                    // Throttle consumer
                    delay(10)
                    return@listen false
                }
            }
            (0..100).forEach {
                channel.offer(it)
                delay(1)
            }
            channel.close()
            val received = deferred.await()
            assertTrue(
                received.size < 20,
                "Received data should be throttled; expected that around 1/10th of all events are consumed"
            )
            println(received)
        }
    }

    @Test
    fun uniqueOnly() {
        val channel = BroadcastChannel<Int>(100)
        runBlocking {
            val fullReceiver = channel.openSubscription()
            val uniqueReceiver = channel.openSubscription().uniqueOnly(this)

            val fullDeferred = async { listen(fullReceiver) }
            val uniqueDeferred = async { listen(uniqueReceiver) }

            listOf(0, 1, 2, 3, 3, 3, 4, 3, 5, 5, 1).forEach {
                channel.offer(it)
            }
            channel.close()

            val fullData = fullDeferred.await()
            val uniqueData = uniqueDeferred.await()

            assertEquals(
                listOf(0, 1, 2, 3, 3, 3, 4, 3, 5, 5, 1),
                fullData,
                "Full receiver should get all channel events"
            )
            assertEquals(
                listOf(0, 1, 2, 3, 4, 3, 5, 1),
                uniqueData,
                "Unique receiver should not have two consecutive events that are equal"
            )
        }
    }
}
