/*
 * Copyright 2019 Allan Wang
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
