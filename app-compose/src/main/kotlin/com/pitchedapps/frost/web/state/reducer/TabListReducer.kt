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
package com.pitchedapps.frost.web.state.reducer

import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.web.state.AuthWebState
import com.pitchedapps.frost.web.state.FrostWebState
import com.pitchedapps.frost.web.state.TabListAction
import com.pitchedapps.frost.web.state.TabListAction.SetHomeTabs
import com.pitchedapps.frost.web.state.TabWebState

internal object TabListReducer {
  fun reduce(state: FrostWebState, action: TabListAction): FrostWebState {
    return when (action) {
      is SetHomeTabs -> {
        val tabs = action.data.mapIndexed { i, fbItem -> fbItem.toTab(i, state.auth) }
        state.copy(homeTabs = tabs)
      }
    }
  }
}

private fun FbItem.toTab(i: Int, auth: AuthWebState): TabWebState =
  TabWebState(
    id = TabWebState.homeTabId(i),
    userId = auth.currentUser,
    baseUrl = url,
    url = url,
    icon = icon,
  )
