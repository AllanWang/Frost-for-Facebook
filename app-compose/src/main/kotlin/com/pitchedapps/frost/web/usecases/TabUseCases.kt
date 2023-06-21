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
package com.pitchedapps.frost.web.usecases

import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.web.state.FrostWebStore
import com.pitchedapps.frost.web.state.TabAction
import javax.inject.Inject

class TabUseCases
@Inject
internal constructor(
  private val store: FrostWebStore,
  val requests: TabRequestUseCases,
  val responses: TabResponseUseCases,
) {
  fun updateUrl(tabId: WebTargetId, url: String) {
    store.dispatch(TabAction(tabId = tabId, action = TabAction.ContentAction.UpdateUrlAction(url)))
  }

  fun updateTitle(tabId: WebTargetId, title: String?) {
    store.dispatch(
      TabAction(
        tabId = tabId,
        action = TabAction.ContentAction.UpdateTitleAction(title),
      ),
    )
  }

  fun updateNavigation(tabId: WebTargetId, canGoBack: Boolean, canGoForward: Boolean) {
    store.dispatch(
      TabAction(
        tabId = tabId,
        action =
          TabAction.ContentAction.UpdateNavigationAction(
            canGoBack = canGoBack,
            canGoForward = canGoForward,
          ),
      ),
    )
  }
}

class TabRequestUseCases @Inject internal constructor(private val store: FrostWebStore) {
  fun requestUrl(tabId: WebTargetId, url: String) {
    store.dispatch(TabAction(tabId = tabId, action = TabAction.UserAction.LoadUrlAction(url)))
  }

  fun goBack(tabId: WebTargetId) {
    store.dispatch(TabAction(tabId = tabId, action = TabAction.UserAction.GoBackAction))
  }

  fun goForward(tabId: WebTargetId) {
    store.dispatch(TabAction(tabId = tabId, action = TabAction.UserAction.GoForwardAction))
  }
}

class TabResponseUseCases @Inject internal constructor(private val store: FrostWebStore) {
  fun respondUrl(tabId: WebTargetId, url: String) {
    store.dispatch(
      TabAction(
        tabId = tabId,
        action = TabAction.ResponseAction.LoadUrlResponseAction(url),
      ),
    )
  }

  fun respondSteps(tabId: WebTargetId, steps: Int) {
    store.dispatch(
      TabAction(
        tabId = tabId,
        action = TabAction.ResponseAction.WebStepResponseAction(steps),
      ),
    )
  }
}
