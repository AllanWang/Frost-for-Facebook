package com.pitchedapps.frost.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@UseExperimental(ExperimentalCoroutinesApi::class)
fun <T> BroadcastChannel<T>.subscribeDuringJob(
    scope: CoroutineScope,
    context: CoroutineContext,
    onReceive: suspend (T) -> Unit
) {
    val receiver = openSubscription()
    scope.launch(context) {
        for (r in receiver) {
            onReceive(r)
        }
    }
    scope.coroutineContext[Job]!!.invokeOnCompletion { receiver.cancel() }
}