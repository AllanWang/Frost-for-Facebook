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
import com.pitchedapps.frost.web.state.state.SessionState
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.Store

/**
 * See
 * https://github.com/mozilla-mobile/firefox-android/blob/main/android-components/components/browser/state/src/main/java/mozilla/components/browser/state/store/BrowserStore.kt
 *
 * For firefox example.
 */
class FrostWebStore(
  initialState: FrostWebState = FrostWebState(),
  frostWebReducer: FrostWebReducer,
  middleware: List<Middleware<FrostWebState, FrostWebAction>> = emptyList(),
) :
  Store<FrostWebState, FrostWebAction>(
    initialState,
    frostWebReducer::reduce,
    middleware,
    "FrostStore",
  ) {
  init {
    dispatch(InitAction)
  }
}

operator fun FrostWebState.get(tabId: WebTargetId): SessionState? {
  if (floatingTab?.id == tabId) return floatingTab
  return homeTabs.find { it.id == tabId }
}
