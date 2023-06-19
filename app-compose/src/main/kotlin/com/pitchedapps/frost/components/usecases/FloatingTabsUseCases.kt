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

import javax.inject.Inject
import javax.inject.Singleton
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore

/** Use cases for the floating tabs, located in an overlay activity. */
@Singleton
class FloatingTabsUseCases @Inject internal constructor(private val store: BrowserStore) {

  /**
   * Create or update the floating tab url.
   *
   * There is at most one floating tab at all times.
   *
   * TODO: Add context id.
   */
  fun createFloatingTab(url: String) {
    if (store.state.findTab(TAB_ID) == null) {
      val tab = createTab(url = url, id = TAB_ID)
      store.dispatch(TabListAction.AddTabAction(tab = tab, select = false))
    }
    store.dispatch(EngineAction.LoadUrlAction(tabId = TAB_ID, url = url))
  }

  /** Remove floating tab screen. */
  fun removeFloatingTab() {
    store.dispatch(TabListAction.RemoveTabAction(tabId = TAB_ID))
  }

  companion object {
    /** Unique tab id for floating screen. */
    const val TAB_ID = "floating_tab_id"
  }
}
