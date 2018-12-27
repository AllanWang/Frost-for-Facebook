package com.pitchedapps.frost.views

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.count
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
 * Collection of tests around the view thread logic
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
class FrostContentViewAsyncTest {

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

    private suspend fun <T> listen(channel: ReceiveChannel<T>, shouldEnd: (T) -> Boolean = { false }): List<T> =
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
}