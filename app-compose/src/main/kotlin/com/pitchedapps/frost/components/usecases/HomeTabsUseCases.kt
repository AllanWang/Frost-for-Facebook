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
package com.pitchedapps.frost.components.usecases

import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.web.state.FrostWebStore
import com.pitchedapps.frost.web.state.TabListAction.SelectHomeTab
import com.pitchedapps.frost.web.state.TabListAction.SetHomeTabs
import javax.inject.Inject

/** Use cases for the home screen. */
class HomeTabsUseCases @Inject internal constructor(private val store: FrostWebStore) {

  /**
   * Create the provided tabs.
   *
   * If there are existing tabs, they will be replaced.
   */
  fun createHomeTabs(items: List<FbItem>) {
    store.dispatch(SetHomeTabs(items))
  }

  /**
   * Select home tab based on index.
   *
   * If the index is OOB, the selected tab will be null.
   */
  fun selectHomeTab(tabId: WebTargetId) {
    store.dispatch(SelectHomeTab(tabId))
  }
}
