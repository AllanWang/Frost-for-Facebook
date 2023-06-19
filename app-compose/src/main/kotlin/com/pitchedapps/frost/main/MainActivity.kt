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
package com.pitchedapps.frost.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.compose.FrostTheme
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.tab
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity.
 *
 * Contains tab layouts for browsing.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    logger.atInfo().log("onCreate main activity")
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val tabs = FbItem.defaults().map { it.tab(this) } // TODO allow custom tabs

    setContent {
      FrostTheme {
        MainScreen(
          tabs = tabs,
        )
      }
    }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
