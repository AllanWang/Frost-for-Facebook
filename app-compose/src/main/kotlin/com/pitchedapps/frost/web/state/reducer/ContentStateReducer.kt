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

import com.pitchedapps.frost.web.state.TabAction.Action
import com.pitchedapps.frost.web.state.TabAction.ContentAction.UpdateNavigationAction
import com.pitchedapps.frost.web.state.TabAction.ContentAction.UpdateProgressAction
import com.pitchedapps.frost.web.state.TabAction.ContentAction.UpdateTitleAction
import com.pitchedapps.frost.web.state.TabAction.ContentAction.UpdateUrlAction
import com.pitchedapps.frost.web.state.TabAction.ResponseAction
import com.pitchedapps.frost.web.state.TabAction.ResponseAction.LoadUrlResponseAction
import com.pitchedapps.frost.web.state.TabAction.ResponseAction.WebStepResponseAction
import com.pitchedapps.frost.web.state.TabAction.UserAction
import com.pitchedapps.frost.web.state.TabAction.UserAction.GoBackAction
import com.pitchedapps.frost.web.state.TabAction.UserAction.GoForwardAction
import com.pitchedapps.frost.web.state.TabAction.UserAction.LoadUrlAction
import com.pitchedapps.frost.web.state.state.ContentState
import com.pitchedapps.frost.web.state.state.TransientWebState
import javax.inject.Inject

internal class ContentStateReducer @Inject internal constructor() {

  fun reduce(state: ContentState, action: Action): ContentState {
    return when (action) {
      is UpdateUrlAction -> state.copy(url = action.url)
      is UpdateProgressAction -> state.copy(progress = action.progress)
      is UpdateNavigationAction ->
        state.copy(
          canGoBack = action.canGoBack,
          canGoForward = action.canGoForward,
        )
      is UpdateTitleAction -> state.copy(title = action.title)
      is UserAction ->
        state.copy(
          transientState =
            FrostTransientWebReducer.reduce(
              state.transientState,
              action,
            ),
        )
      is ResponseAction ->
        state.copy(
          transientState =
            FrostTransientFulfillmentWebReducer.reduce(
              state.transientState,
              action,
            ),
        )
    }
  }
}

private object FrostTransientWebReducer {
  fun reduce(state: TransientWebState, action: UserAction): TransientWebState {
    return when (action) {
      is LoadUrlAction -> state.copy(targetUrl = action.url)
      is GoBackAction -> state.copy(navStep = state.navStep - 1)
      is GoForwardAction -> state.copy(navStep = state.navStep + 1)
    }
  }
}

private object FrostTransientFulfillmentWebReducer {
  fun reduce(state: TransientWebState, action: ResponseAction): TransientWebState {
    return when (action) {
      is LoadUrlResponseAction ->
        if (state.targetUrl == action.url) state.copy(targetUrl = null) else state
      is WebStepResponseAction -> state.copy(navStep = state.navStep - action.steps)
    }
  }
}
