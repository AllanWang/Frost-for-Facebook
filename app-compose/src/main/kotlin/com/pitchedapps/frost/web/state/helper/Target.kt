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
package com.pitchedapps.frost.web.state.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.web.state.FrostWebState
import com.pitchedapps.frost.web.state.FrostWebStore
import com.pitchedapps.frost.web.state.TabWebState
import mozilla.components.lib.state.Store
import mozilla.components.lib.state.ext.observeAsComposableState

/**
 * Helper for allowing a component consumer to specify which tab a component should target.
 *
 * Based off of mozilla.components.browser.state.helper.Target:
 * https://github.com/mozilla-mobile/firefox-android/blob/main/android-components/components/browser/state/src/main/java/mozilla/components/browser/state/helper/Target.kt
 */
sealed class Target {
  /**
   * Looks up this target in the given [FrostWebStore] and returns the matching [TabWebState] if
   * available. Otherwise returns `null`.
   *
   * @param store to lookup this target in.
   */
  fun lookupIn(store: FrostWebStore): TabWebState? = lookupIn(store.state)

  /**
   * Looks up this target in the given [FrostWebState] and returns the matching [TabWebState] if
   * available. Otherwise returns `null`.
   *
   * @param state to lookup this target in.
   */
  abstract fun lookupIn(state: FrostWebState): TabWebState?

  /**
   * Observes this target and represents the mapped state (using [map]) via [State].
   *
   * Everytime the [Store] state changes and the result of the [observe] function changes for this
   * state, the returned [State] will be updated causing recomposition of every [State.value] usage.
   *
   * The [Store] observer will automatically be removed when this composable disposes or the current
   * [LifecycleOwner] moves to the [Lifecycle.State.DESTROYED] state.
   *
   * @param store that should get observed
   * @param observe function that maps a [TabWebState] to the (sub) state that should get observed
   *   for changes.
   */
  @Composable
  fun <R> observeAsComposableStateFrom(
    store: FrostWebStore,
    observe: (TabWebState?) -> R,
  ): State<TabWebState?> {
    return store.observeAsComposableState(
      map = { state -> lookupIn(state) },
      observe = { state -> observe(lookupIn(state)) },
    )
  }

  data class HomeTab(val id: WebTargetId) : Target() {
    override fun lookupIn(state: FrostWebState): TabWebState? {
      return state.homeTabs.find { it.id == id }
    }
  }

  object FloatingTab : Target() {
    override fun lookupIn(state: FrostWebState): TabWebState? {
      return state.floatingTab
    }
  }
}
