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

import java.util.concurrent.Executors
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Collection of tests around coroutines */
class CoroutineTest {

  private fun <T : Any> SharedFlow<T?>.takeUntilNull(): Flow<T> =
    takeWhile { it != null }.filterNotNull()

  /** Sanity check to ensure that contexts are being honoured */
  @Test
  fun contextSwitching() {
    val mainTag = "main-test"
    val mainDispatcher =
      Executors.newSingleThreadExecutor { r -> Thread(r, mainTag) }.asCoroutineDispatcher()

    val flow = MutableSharedFlow<String?>(100)
    runBlocking(Dispatchers.IO) {
      launch(mainDispatcher) {
        flow.takeUntilNull().collect { thread ->
          assertTrue(
            Thread.currentThread().name.startsWith(mainTag),
            "Channel should be received in main thread"
          )
          assertFalse(thread.startsWith(mainTag), "Channel execution should not be in main thread")
        }
      }
      listOf(EmptyCoroutineContext, Dispatchers.IO, Dispatchers.Default, Dispatchers.IO)
        .map { async(it) { flow.emit(Thread.currentThread().name) } }
        .joinAll()
      flow.emit(null)
      val count = flow.takeUntilNull().count()
      assertEquals(4, count, "Not all events received")
    }
  }
}
