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
import com.pitchedapps.frost.web.state.reducer.TabListReducer
import com.pitchedapps.frost.web.state.state.FloatingTabSessionState
import com.pitchedapps.frost.web.state.state.HomeTabSessionState
import com.pitchedapps.frost.web.state.state.SessionState
import javax.inject.Inject

/**
 * See
 * https://github.com/mozilla-mobile/firefox-android/blob/main/android-components/components/browser/state/src/main/java/mozilla/components/browser/state/reducer/BrowserStateReducer.kt
 *
 * For firefox example
 */
class FrostWebReducer
@Inject
internal constructor(
  private val tabListReducer: TabListReducer,
  private val contentStateReducer: ContentStateReducer
) {
  fun reduce(state: FrostWebState, action: FrostWebAction): FrostWebState {
    return when (action) {
      is InitAction -> state
      is TabListAction -> tabListReducer.reduce(state, action)
      is TabAction ->
        state.updateTabState(action.tabId) { session ->
          val newContent = contentStateReducer.reduce(session.content, action.action)
          session.createCopy(content = newContent)
        }
    }
  }
}

@Suppress("Unchecked_Cast")
internal fun FrostWebState.updateTabState(
  tabId: WebTargetId,
  update: (SessionState) -> SessionState,
): FrostWebState {
  val floatingTabMatch = floatingTab?.takeIf { it.id == tabId }
  if (floatingTabMatch != null)
    return copy(floatingTab = update(floatingTabMatch) as FloatingTabSessionState)

  val newHomeTabs = homeTabs.updateTabs(tabId, update) as List<HomeTabSessionState>?
  if (newHomeTabs != null) return copy(homeTabs = newHomeTabs)
  return this
}

/**
 * Finds the corresponding tab in the list and replaces it using [update].
 *
 * @param tabId ID of the tab to change.
 * @param update Returns a new version of the tab state.
 */
internal fun <T : SessionState> List<T>.updateTabs(
  tabId: WebTargetId,
  update: (T) -> T,
): List<SessionState>? {
  val tabIndex = indexOfFirst { it.id == tabId }
  if (tabIndex == -1) return null
  return subList(0, tabIndex) + update(get(tabIndex)) + subList(tabIndex + 1, size)
}
