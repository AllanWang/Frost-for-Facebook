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
package com.pitchedapps.frost.web.state

import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.web.state.reducer.ContentStateReducer

/**
 * See
 * https://github.com/mozilla-mobile/firefox-android/blob/main/android-components/components/browser/state/src/main/java/mozilla/components/browser/state/reducer/BrowserStateReducer.kt
 *
 * For firefox example
 */
internal object FrostWebReducer {
  fun reduce(state: FrostWebState, action: FrostWebAction): FrostWebState {
    return when (action) {
      is InitAction -> state
      is TabAction ->
        state.updateTabState(action.tabId) { ContentStateReducer.reduce(it, action.action) }
    }
  }
}

internal fun FrostWebState.updateTabState(
  tabId: WebTargetId,
  update: (TabWebState) -> TabWebState,
): FrostWebState {
  val floatingTabMatch = floatingTab?.takeIf { it.id == tabId }
  if (floatingTabMatch != null) return copy(floatingTab = update(floatingTabMatch))

  val newHomeTabs = homeTabs.updateTabs(tabId, update)
  if (newHomeTabs != null) return copy(homeTabs = newHomeTabs)
  return this
}

/**
 * Finds the corresponding tab in the list and replaces it using [update].
 *
 * @param tabId ID of the tab to change.
 * @param update Returns a new version of the tab state.
 */
internal fun List<TabWebState>.updateTabs(
  tabId: WebTargetId,
  update: (TabWebState) -> TabWebState,
): List<TabWebState>? {
  val tabIndex = indexOfFirst { it.id == tabId }
  if (tabIndex == -1) return null

  return subList(0, tabIndex) + update(get(tabIndex)) + subList(tabIndex + 1, size)
}
