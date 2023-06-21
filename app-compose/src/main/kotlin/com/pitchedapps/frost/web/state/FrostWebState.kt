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

import com.pitchedapps.frost.ext.FrostAccountId
import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.web.state.state.FloatingTabSessionState
import com.pitchedapps.frost.web.state.state.HomeTabSessionState
import mozilla.components.lib.state.State

/**
 * See
 * https://github.com/mozilla-mobile/firefox-android/blob/main/android-components/components/browser/state/src/main/java/mozilla/components/browser/state/state/BrowserState.kt
 *
 * for Firefox example.
 */
data class FrostWebState(
  val auth: AuthWebState = AuthWebState(),
  val selectedHomeTab: WebTargetId? = null,
  val homeTabs: List<HomeTabSessionState> = emptyList(),
  var floatingTab: FloatingTabSessionState? = null,
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
