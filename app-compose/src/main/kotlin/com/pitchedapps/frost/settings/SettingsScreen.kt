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
package com.pitchedapps.frost.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pitchedapps.frost.R
import com.pitchedapps.frost.compose.FrostPreview
import com.pitchedapps.frost.settings.screens.MainSettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
  val navController = rememberNavController()

  val topBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Scaffold(
    topBar = {
      MediumTopAppBar(
        scrollBehavior = topBarScrollBehavior,
        navigationIcon = {
          IconButton(onClick = {}) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
          }
        },
        title = {
          val entry by navController.currentBackStackEntryAsState()

          val title =
            entry
              ?.destination
              ?.route
              ?.let { SettingsPages.valueOf(it) }
              ?.titleId
              ?.let { stringResource(id = it) }

          if (title != null) {
            Text(text = title)
          }
        },
        actions = { IconButton(onClick = {}) { Icon(Icons.Outlined.Info, null) } },
      )
    },
  ) { paddingValue ->
    NavHost(
      modifier =
        Modifier.fillMaxSize()
          .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
          .padding(paddingValue),
      navController = navController,
      startDestination = SettingsPages.Main.name,
    ) {
      composable(SettingsPages.Main) { MainSettingsScreen() }
      /*...*/
    }
  }
}

private fun NavGraphBuilder.composable(
  route: SettingsPages,
  content: @Composable (NavBackStackEntry) -> Unit
) = composable(route = route.name, content = content)

private enum class SettingsPages(val titleId: Int) {
  Main(R.string.settings),
  Appearance(R.string.appearance)
}

@Preview
@Composable
fun SettingsScreenPreview() {
  FrostPreview { SettingsScreen() }
}
