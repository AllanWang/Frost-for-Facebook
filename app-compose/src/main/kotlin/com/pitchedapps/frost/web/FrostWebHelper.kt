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
package com.pitchedapps.frost.web

import com.pitchedapps.frost.facebook.isFacebookUrl
import com.pitchedapps.frost.facebook.isMessengerUrl
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class FrostWebHelper @Inject internal constructor(frostAdBlock: Optional<FrostAdBlock>) {
  private val frostAdBlock = frostAdBlock.getOrNull()

  /** Returns true if url should be intercepted (replaced with blank resource) */
  fun shouldInterceptUrl(url: String): Boolean {
    val httpUrl = url.toHttpUrlOrNull() ?: return false
    val host = httpUrl.host
    if (host.contains("facebook") || host.contains("fbcdn")) return false
    if (frostAdBlock?.isAdHost(host) == true) return true
    return false
  }

  /**
   * Returns true if url should allow refreshes.
   *
   * Some urls are known to be invalid entrypoints, and cannot be refreshed. Others may contain
   * editable data without save state, so disabling swipe to refresh is preferred.
   */
  fun allowUrlSwipeToRefresh(url: String): Boolean {
    if (url.isMessengerUrl) return false
    if (!url.isFacebookUrl) return true
    if (url.contains("soft=composer")) return false
    if (url.contains("sharer.php") || url.contains("sharer-dialog.php")) return false
    return true
  }
}
