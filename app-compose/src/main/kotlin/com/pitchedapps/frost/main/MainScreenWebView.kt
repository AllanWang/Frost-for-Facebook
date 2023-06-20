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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pitchedapps.frost.web.state.FrostWebStore
import com.pitchedapps.frost.webview.FrostWebComposer
import mozilla.components.lib.state.ext.observeAsState

@Composable
fun MainScreenWebView(modifier: Modifier = Modifier) {
  val vm: MainScreenViewModel = viewModel()
  Scaffold(
    modifier = modifier,
    topBar = { MainTopBar(modifier = modifier) },
  ) { paddingValues ->
    MainScreenWebContainer(
      modifier = Modifier.padding(paddingValues),
      store = vm.store,
      frostWebComposer = vm.frostWebComposer,
    )
  }
}

@Composable
private fun MainScreenWebContainer(
  modifier: Modifier,
  store: FrostWebStore,
  frostWebComposer: FrostWebComposer
) {
  val homeTabs by store.observeAsState(initialValue = emptyList()) { it.homeTabs }
  val homeTabComposables = remember(homeTabs) { homeTabs.map { frostWebComposer.create(it.id) } }

  Box(modifier = modifier) { homeTabComposables.firstOrNull()?.WebView() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(modifier: Modifier = Modifier) {
  TopAppBar(title = { Text(text = "Title") })
}
