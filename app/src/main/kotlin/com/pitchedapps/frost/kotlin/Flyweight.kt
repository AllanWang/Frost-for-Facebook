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

import com.pitchedapps.frost.utils.L
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.concurrent.ConcurrentHashMap

/**
 * Flyweight to keep track of values so long as they are valid.
 * Values that have been fetched within [maxAge] from the time of use will be reused.
 * If multiple requests are sent with the same key, then the value should only be fetched once.
 * Otherwise, they will be fetched using [fetcher].
 * All requests will stem from the supplied [scope].
 */
class Flyweight<K, V>(
    val scope: CoroutineScope,
    val maxAge: Long,
    private val fetcher: suspend (K) -> V
) {

    // Receives a key and a pending request
    private val actionChannel = Channel<Pair<K, CompletableDeferred<V>>>(Channel.UNLIMITED)
    // Receives a key to invalidate the associated value
    private val invalidatorChannel = Channel<K>(Channel.UNLIMITED)
    // Receives a key and the resulting value
    private val receiverChannel = Channel<Pair<K, Result<V>>>(Channel.UNLIMITED)

    // Keeps track of keys and associated update times
    private val conditionMap: MutableMap<K, Long> = mutableMapOf()
    // Keeps track of keys and associated values
    private val resultMap: MutableMap<K, Result<V>> = mutableMapOf()
    // Keeps track of unfulfilled actions
    // Note that the explicit type is very important here. See https://youtrack.jetbrains.net/issue/KT-18053
    private val pendingMap: MutableMap<K, MutableList<CompletableDeferred<V>>> = ConcurrentHashMap()

    private val job: Job

    private fun CompletableDeferred<V>.completeWith(result: Result<V>) {
        if (result.isSuccess)
            complete(result.getOrNull()!!)
        else
            completeExceptionally(result.exceptionOrNull()!!)
    }

    private val errHandler =
        CoroutineExceptionHandler { _, throwable -> L.d { "FbAuth failed ${throwable.message}" } }

    init {
        job =
            scope.launch(Dispatchers.IO + SupervisorJob() + errHandler) {
                launch {
                    while (isActive) {
                        select<Unit> {
                            /*
                             * New request received. Continuation should be fulfilled eventually
                             */
                            actionChannel.onReceive { (key, completable) ->
                                val lastUpdate = conditionMap[key]
                                val lastResult = resultMap[key]
                                // Valid value, retrieved within acceptable time
                                if (lastResult != null && lastUpdate != null && System.currentTimeMillis() - lastUpdate < maxAge) {
                                    completable.completeWith(lastResult)
                                } else {
                                    val valueRequestPending = key in pendingMap
                                    pendingMap.getOrPut(key) { mutableListOf() }.add(completable)
                                    if (!valueRequestPending)
                                        fulfill(key)
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
                                    fulfill(key)
                            }
                            /*
                             * Value request fulfilled. Should now fulfill pending requests
                             */
                            receiverChannel.onReceive { (key, result) ->
                                conditionMap[key] = System.currentTimeMillis()
                                resultMap[key] = result
                                pendingMap.remove(key)?.forEach {
                                    it.completeWith(result)
                                }
                            }
                        }
                    }
                }
            }
    }

    /*
     * Value request received. Should fetch new value using supplied fetcher
     */
    private fun fulfill(key: K) {
        scope.launch {
            val result = runCatching {
                fetcher(key)
            }
            receiverChannel.send(key to result)
        }
    }

    /**
     * Queues the request, and returns a completable once it is sent to a channel.
     * The fetcher will only be suspended if the channels are full.
     *
     * Note that if the job is already inactive, a cancellation exception will be thrown.
     * The message may default to the message for all completables under a cancelled job
     */
    fun fetch(key: K): CompletableDeferred<V> {
        val completable = CompletableDeferred<V>(job)
        if (!job.isActive) completable.completeExceptionally(CancellationException("Flyweight is not active"))
        else actionChannel.offer(key to completable)
        return completable
    }

    suspend fun invalidate(key: K) {
        invalidatorChannel.send(key)
    }

    fun cancel() {
        job.cancel()
        if (pendingMap.isNotEmpty()) {
            val error = CancellationException("Flyweight cancelled")
            pendingMap.values.flatten().forEach { it.completeExceptionally(error) }
            pendingMap.clear()
        }
        actionChannel.close()
        invalidatorChannel.close()
        receiverChannel.close()
        conditionMap.clear()
        resultMap.clear()
    }
}
