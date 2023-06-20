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

import androidx.compose.ui.graphics.vector.ImageVector
import com.pitchedapps.frost.ext.FrostAccountId
import com.pitchedapps.frost.ext.WebTargetId
import mozilla.components.lib.state.State

/**
 * See
 * https://github.com/mozilla-mobile/firefox-android/blob/main/android-components/components/browser/state/src/main/java/mozilla/components/browser/state/state/BrowserState.kt
 *
 * for Firefox example.
 */
data class FrostWebState(
  val auth: AuthWebState = AuthWebState(),
  val homeTabs: List<TabWebState> = emptyList(),
  var floatingTab: TabWebState? = null,
) : State

/**
 * Auth web state.
 *
 * Unlike GeckoView, WebView currently has a singleton cookie manager.
 *
 * Cookies are tied to the entire app, rather than per tab.
 *
 * @param currentUser User based on loaded cookies
 * @param homeUser User selected for home screen
 */
data class AuthWebState(
  val currentUser: AuthUser = AuthUser.Unknown,
  val homeUser: AuthUser = AuthUser.Unknown,
) {
  sealed interface AuthUser {
    data class User(val id: FrostAccountId) : AuthUser

    data class Transitioning(val targetId: FrostAccountId?) : AuthUser

    object LoggedOut : AuthUser

    object Unknown : AuthUser
  }
}

data class TabWebState(
  val id: WebTargetId,
  val userId: AuthWebState.AuthUser,
  val baseUrl: String,
  val url: String,
  val icon: ImageVector? = null,
  val title: String? = null,
  val progress: Int = 100,
  val loading: Boolean = false,
  val canGoBack: Boolean = false,
  val canGoForward: Boolean = false,
  val transientState: TransientWebState = TransientWebState(),
) {
  companion object {
    fun homeTabId(index: Int): WebTargetId = WebTargetId("home-tab--$index")

    val FLOATING_TAB_ID = WebTargetId("floating-tab")
  }
}

/**
 * Transient web state.
 *
 * While we typically don't want to store this, our webview is not a composable, and requires a
 * bridge to handle events.
 *
 * This state is not a list of pending actions, but rather a snapshot of the expected changes so
 * that conflicting events can be ignored.
 *
 * @param targetUrl url destination if nonnull
 * @param navStep pending steps. Positive = steps forward, negative = steps backward
 */
data class TransientWebState(
  val targetUrl: String? = null,
  val navStep: Int = 0,
)
