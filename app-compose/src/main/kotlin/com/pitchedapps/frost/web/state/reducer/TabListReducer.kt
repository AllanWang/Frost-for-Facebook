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

import android.content.Context
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.tab
import com.pitchedapps.frost.web.state.AuthWebState
import com.pitchedapps.frost.web.state.FrostWebState
import com.pitchedapps.frost.web.state.TabListAction
import com.pitchedapps.frost.web.state.TabListAction.SetHomeTabs
import com.pitchedapps.frost.web.state.state.ContentState
import com.pitchedapps.frost.web.state.state.HomeTabSessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class TabListReducer
@Inject
internal constructor(
  @ApplicationContext private val context: Context,
) {
  fun reduce(state: FrostWebState, action: TabListAction): FrostWebState {
    return when (action) {
      is SetHomeTabs -> {
        val tabs =
          action.data.mapIndexed { i, fbItem -> fbItem.toHomeTabSession(context, i, state.auth) }
        val selectedTab = action.selectedTab?.let { HomeTabSessionState.homeTabId(it) }
        state.copy(
          homeTabs = tabs,
          selectedHomeTab = selectedTab,
        )
      }
      is TabListAction.SelectHomeTab -> state.copy(selectedHomeTab = action.id)
    }
  }
}

private fun FbItem.toHomeTabSession(
  context: Context,
  i: Int,
  auth: AuthWebState
): HomeTabSessionState =
  HomeTabSessionState(
    userId = auth.currentUser,
    content = ContentState(url = url),
    tab = tab(context, id = HomeTabSessionState.homeTabId(i)),
  )
