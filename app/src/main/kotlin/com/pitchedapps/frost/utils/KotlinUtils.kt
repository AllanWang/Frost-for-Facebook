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
