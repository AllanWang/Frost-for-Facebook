/*
 * Copyright 2023 Allan Wang
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
package com.pitchedapps.frost.web

import com.pitchedapps.frost.components.FrostDataStore
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Singleton

/** Snapshot of UI options based on user preferences */
interface FrostWebUiOptions {

  val theme: Theme

  enum class Theme {
    Original,
    Light,
    Dark,
    Amoled,
    Glass // Custom
  }
}

/**
 * Singleton to provide snapshots of [FrostWebUiOptions].
 *
 * This is a mutable class, and does not provide change listeners. We will update activities
 * manually when needed.
 */
@Singleton
class FrostWebUiSnapshot(private val dataStore: FrostDataStore) {

  @Volatile
  var options: FrostWebUiOptions = defaultOptions()
    private set

  private val stale = AtomicBoolean(true)

  private fun defaultOptions(): FrostWebUiOptions =
    object : FrostWebUiOptions {
      override val theme: FrostWebUiOptions.Theme = FrostWebUiOptions.Theme.Original
    }

  /** Fetch new snapshot and update other singletons */
  suspend fun reload() {
    if (!stale.getAndSet(false)) return
    // todo load
  }

  fun markAsStale() {
    stale.set(true)
  }
}
