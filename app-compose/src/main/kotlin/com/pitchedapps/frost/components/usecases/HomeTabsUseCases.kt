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

import com.google.common.flogger.FluentLogger
import javax.inject.Inject
import javax.inject.Singleton
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext

@Singleton
class HomeTabsUseCases @Inject internal constructor(private val store: BrowserStore) {
  fun createHomeTabs(
    contextId: String,
    selectedIndex: Int,
    urls: List<String>
  ): List<TabSessionState> {
    store.dispatch(TabListAction.RemoveAllTabsAction())
    if (urls.isEmpty()) return emptyList()
    val tabs =
      urls.mapIndexed { i, url -> createTab(id = tabId(i), url = url, contextId = contextId) }
    store.dispatch(TabListAction.AddMultipleTabsAction(tabs))
    // Preload all tabs
    for (tab in tabs) {
      store.dispatch(EngineAction.LoadUrlAction(tab.id, tab.content.url))
      //      if (tab.content.url == MESSENGER_URL) {
      //        store.dispatch(EngineAction.ToggleDesktopModeAction(tab.id, enable = true))
      //      }
    }
    selectHomeTab(selectedIndex)
    return tabs
  }

  /**
   * Select home tab based on index.
   *
   * If the index is OOB, the selected tab will be null.
   */
  fun selectHomeTab(index: Int) {
    store.dispatch(TabListAction.SelectTabAction(tabId(index)))
  }

  fun reloadTab(index: Int) {
    store.dispatch(EngineAction.ReloadAction(tabId(index)))
  }

  // Cannot use injection
  class HomeMiddleware : Middleware<BrowserState, BrowserAction> {
    override fun invoke(
      context: MiddlewareContext<BrowserState, BrowserAction>,
      next: (BrowserAction) -> Unit,
      action: BrowserAction
    ) {
      //      if (action is ContentAction.UpdateUrlAction) {
      //        logger.atInfo().log("url update %s", action)
      //        action.sessionId
      //        return
      //      }
      next(action)
      //      when (action) {
      //        is ContentAction.UpdateUrlAction -> {
      //          logger.atInfo().log("url update %s", action)
      //          action.sessionId
      //        }
      //        else -> next(action)
      //      }
    }

    companion object {
      private val logger = FluentLogger.forEnclosingClass()
    }
  }

  companion object {
    private const val PREFIX = "frost-home"

    private fun isHomeTab(id: String): Boolean = id.startsWith(PREFIX)

    private fun tabId(index: Int) = "$PREFIX--$index"
  }
}
