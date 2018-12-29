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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Flyweight to keep track of values so long as they are valid.
 * Values that have been fetched within [maxAge] from the time of use will be reused.
 * If multiple requests are sent with the same key, then the value should only be fetched once.
 * Otherwise, they will be fetched using [fetcher].
 * All requests will stem from the supplied [scope].
 */
class Flyweight<K, V>(
    val scope: CoroutineScope,
    capacity: Int,
    val maxAge: Long,
    private val fetcher: suspend (K) -> V
) {

    // Receives a key and a pending request
    private val actionChannel = Channel<Pair<K, Continuation<V>>>(capacity)
    // Receives a key to invalidate the associated value
    private val invalidatorChannel = Channel<K>(capacity)
    // Receives a key to fetch the value
    private val requesterChannel = Channel<K>(capacity)
    // Receives a key and the resulting value
    private val receiverChannel = Channel<Pair<K, Result<V>>>(capacity)

    // Keeps track of keys and associated update times
    private val conditionMap: MutableMap<K, Long> = mutableMapOf()
    // Keeps track of keys and associated values
    private val resultMap: MutableMap<K, Result<V>> = mutableMapOf()
    // Keeps track of unfulfilled actions
    // Note that the explicit type is very important here. See https://youtrack.jetbrains.net/issue/KT-18053
    private val pendingMap: MutableMap<K, MutableList<Continuation<V>>> = ConcurrentHashMap()

    private val job: Job

    init {
        job = scope.launch(Dispatchers.IO) {
            launch {
                while (isActive) {
                    select<Unit> {
                        /*
                         * New request received. Continuation should be fulfilled eventually
                         */
                        actionChannel.onReceive { (key, continuation) ->
                            val lastUpdate = conditionMap[key]
                            val lastResult = resultMap[key]
                            // Valid value, retrieved within acceptable time
                            if (lastResult != null && lastUpdate != null && System.currentTimeMillis() - lastUpdate < maxAge) {
                                continuation.resumeWith(lastResult)
                            } else {
                                val valueRequestPending = key in pendingMap
                                pendingMap.getOrPut(key) { mutableListOf() }.add(continuation)
                                if (!valueRequestPending)
                                    requesterChannel.send(key)
                            }
                        }
                        /*
                         * Invalidator received. Existing result associated with key should not be used.
                         * Note that any unfulfilled request and future requests should still operate, but with a new value.
                         */
                        invalidatorChannel.onReceive { key ->
                            if (key !in resultMap) {
                                // Nothing to invalidate.
                                // If pending requests exist, they are already in the process of being updated.
                                return@onReceive
                            }
                            conditionMap.remove(key)
                            resultMap.remove(key)
                            if (pendingMap[key]?.isNotEmpty() == true)
                            // Refetch value for pending requests
                                requesterChannel.send(key)
                        }
                        /*
                         * Value request fulfilled. Should now fulfill pending requests
                         */
                        receiverChannel.onReceive { (key, result) ->
                            conditionMap[key] = System.currentTimeMillis()
                            resultMap[key] = result
                            pendingMap.remove(key)?.forEach {
                                it.resumeWith(result)
                            }
                        }
                    }
                }
            }
            launch {
                /*
                 * Value request received. Should fetch new value using supplied fetcher
                 */
                for (key in requesterChannel) {
                    val result = runCatching {
                        fetcher(key)
                    }
                    receiverChannel.send(key to result)
                }
            }
        }
    }

    suspend fun fetch(key: K): V = suspendCoroutine {
        if (!job.isActive) it.resumeWithException(IllegalStateException("Flyweight is not active"))
        else scope.launch {
            actionChannel.send(key to it)
        }
    }

    suspend fun invalidate(key: K) {
        invalidatorChannel.send(key)
    }

    fun cancel() {
        job.cancel()
        if (pendingMap.isNotEmpty()) {
            val error = CancellationException("Flyweight cancelled")
            pendingMap.values.flatten().forEach { it.resumeWithException(error) }
            pendingMap.clear()
        }
        actionChannel.close()
        invalidatorChannel.close()
        requesterChannel.close()
        receiverChannel.close()
        conditionMap.clear()
        resultMap.clear()
    }
}
