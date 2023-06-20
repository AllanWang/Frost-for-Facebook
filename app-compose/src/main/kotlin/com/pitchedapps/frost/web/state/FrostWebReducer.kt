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
      is UserAction.LoadUrlAction -> state.copy(targetUrl = action.url)
      is UserAction.GoBackAction -> state.copy(navStep = state.navStep - 1)
      is UserAction.GoForwardAction -> state.copy(navStep = state.navStep + 1)
    }
  }
}

private object FrostTransientFulfillmentWebReducer {
  fun reduce(state: TransientWebState, action: ResponseAction): TransientWebState {
    return when (action) {
      is ResponseAction.LoadUrlResponseAction ->
        if (state.targetUrl == action.url) state.copy(targetUrl = null) else state
      is ResponseAction.WebStepResponseAction -> state.copy(navStep = state.navStep - action.steps)
    }
  }
}
