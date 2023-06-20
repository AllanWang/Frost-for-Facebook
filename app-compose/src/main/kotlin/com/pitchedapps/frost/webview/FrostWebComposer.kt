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
package com.pitchedapps.frost.webview

import com.pitchedapps.frost.compose.webview.FrostChromeClient
import com.pitchedapps.frost.compose.webview.FrostWebCompose
import com.pitchedapps.frost.compose.webview.FrostWebViewClient
import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.web.FrostWebHelper
import com.pitchedapps.frost.web.state.FrostWebStore
import javax.inject.Inject

class FrostWebComposer
@Inject
internal constructor(
  private val store: FrostWebStore,
  private val webHelper: FrostWebHelper,
) {

  fun create(tabId: WebTargetId): FrostWebCompose {
    val client = FrostWebViewClient(tabId, store, webHelper)
    val chromeClient = FrostChromeClient(tabId, store)
    return FrostWebCompose(tabId, store, client, chromeClient)
  }
}
