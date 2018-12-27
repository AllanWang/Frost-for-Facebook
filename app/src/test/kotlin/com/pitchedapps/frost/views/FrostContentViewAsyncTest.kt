package com.pitchedapps.frost.views

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Collection of tests around the view thread logic
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
class FrostContentViewAsyncTest {

    /**
     * Single threaded dispatcher with thread name "main"
     * Mimics the usage of Android's main dispatcher
     */
    private lateinit var mainDispatcher: ExecutorCoroutineDispatcher

    @BeforeTest
    fun before() {
        mainDispatcher = Executors.newSingleThreadExecutor { r ->
            Thread(r, "main")
        }.asCoroutineDispatcher()
    }

    @AfterTest
    fun after() {
        mainDispatcher.close()
    }

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
}