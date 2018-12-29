package com.pitchedapps.frost.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.isActive

@UseExperimental(ExperimentalCoroutinesApi::class)
fun <T> ReceiveChannel<T>.uniqueOnly(scope: CoroutineScope): ReceiveChannel<T> = scope.produce {
    var previous: T? = null
    for (current in this@uniqueOnly) {
        if (!scope.isActive) {
            cancel()
        } else if (previous != current) {
            previous = current
            send(current)
        }
    }
}