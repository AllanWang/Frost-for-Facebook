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
package com.pitchedapps.frost.overlay

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.compose.FrostTheme
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@AndroidEntryPoint
class WebOverlayActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logger.atInfo().log("onCreate overlay activity")
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent { FrostTheme { WebOverlayScreen() } }
  }

  /**
   * Attempts to parse the action url Returns [true] if no action exists or if the action has been
   * consumed, [false] if we need to notify the user of a bad action
   *
   * TODO Check if share action needs to be merged? Looks like this is both for sharing out of Frost
   * and sharing into Frost
   */
  private fun Intent.parseActionSend(): Boolean {
    if (action != Intent.ACTION_SEND || type != "text/plain") return true
    val text = getStringExtra(Intent.EXTRA_TEXT) ?: return true
    val url = text.toHttpUrlOrNull()?.toString()
    return if (url == null) {
      logger.atInfo().log("Attempted to share a non-url")
      logger.atFinest().log("Shared text %s", text)
      // finish?
      finish()
      //      intent.putExtra(ARG_URL, FbItem.FEED.url)
      false
    } else {
      logger.atInfo().log("Sharing url through overlay")
      logger.atFinest().log("Shared url %s", url)
      //      putExtra(ARG_URL, "${FB_URL_BASE}sharer/sharer.php?u=$url")
      true
    }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
