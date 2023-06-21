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
package com.pitchedapps.frost.web.state.state

import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.main.MainTabItem
import com.pitchedapps.frost.web.state.AuthWebState.AuthUser

/** Data representation of single session. */
interface SessionState {
  val id: WebTargetId
  val userId: AuthUser
  val content: ContentState

  fun createCopy(
    id: WebTargetId = this.id,
    userId: AuthUser = this.userId,
    content: ContentState = this.content
  ): SessionState
}

/** Session for home screen, which includes nav bar data */
data class HomeTabSessionState(
  override val userId: AuthUser,
  override val content: ContentState,
  val tab: MainTabItem,
) : SessionState {

  override val id: WebTargetId
    get() = tab.id

  override fun createCopy(id: WebTargetId, userId: AuthUser, content: ContentState) =
    copy(userId = userId, content = content, tab = tab.copy(id = id))

  companion object {
    fun homeTabId(index: Int): WebTargetId = WebTargetId("home-tab--$index")
  }
}

data class FloatingTabSessionState(
  override val id: WebTargetId,
  override val userId: AuthUser,
  override val content: ContentState,
) : SessionState {
  override fun createCopy(id: WebTargetId, userId: AuthUser, content: ContentState) =
    copy(id = id, userId = userId, content = content)
}

/** Data relating to webview content */
data class ContentState(
  val url: String,
  val title: String? = null,
  val progress: Int = 0,
  val loading: Boolean = false,
  val canGoBack: Boolean = false,
  val canGoForward: Boolean = false,
  val transientState: TransientWebState = TransientWebState(),
)

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
