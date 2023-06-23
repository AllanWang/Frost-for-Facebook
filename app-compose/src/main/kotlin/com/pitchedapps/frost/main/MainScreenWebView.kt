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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pitchedapps.frost.compose.webview.FrostWebCompose
import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.web.state.FrostWebStore
import com.pitchedapps.frost.webview.FrostWebComposer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.components.lib.state.ext.observeAsState

@Composable
fun MainScreenWebView(modifier: Modifier = Modifier) {
  val vm: MainScreenViewModel = viewModel()

  val homeTabs =
    vm.store.observeAsState(initialValue = null) { it.homeTabs.map { it.tab } }.value ?: return

  val selectedHomeTab by vm.store.observeAsState(initialValue = null) { it.selectedHomeTab }

  MainScreenContainer(
    drawerItems = emptyList(),
    drawerSelectedIndex = -1,
    drawerOnSelect = {},
    navItems = homeTabs.map { it.toTab() },
    navSelectedIndex = selectedHomeTab?.let { id -> homeTabs.indexOfFirst { it.id == id } } ?: -1,
    navOnSelect = { vm.useCases.homeTabs.selectHomeTab((homeTabs[it].id)) },
  ) { paddingValues, nestedScrollConnection ->
    MainScreenWebContainer(
      modifier = Modifier.padding(paddingValues).nestedScroll(nestedScrollConnection),
      selectedTab = selectedHomeTab,
      items = homeTabs,
      store = vm.store,
      frostWebComposer = vm.frostWebComposer,
    )
  }
}

@Composable
private fun MainScreenWebContainer(
  modifier: Modifier,
  selectedTab: WebTargetId?,
  items: List<MainTabItem>,
  store: FrostWebStore,
  frostWebComposer: FrostWebComposer
) {
  val homeTabComposables = remember(items) { items.map { frostWebComposer.create(it.id) } }

  PullRefresh(
    modifier = modifier,
    store = store,
  ) {
    MainPager(selectedTab, items = homeTabComposables)
    //    homeTabComposables.find { it.tabId == selectedTab }?.WebView()

    //    MultiViewContainer(store = store)

    //    SampleContainer(selectedTab = selectedTab, items = items)
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPager(selectedTab: WebTargetId?, items: List<FrostWebCompose>) {

  val pagerState = rememberPagerState { items.size }

  LaunchedEffect(selectedTab, items) {
    val i = items.indexOfFirst { it.tabId == selectedTab }
    if (i != -1) {
      pagerState.scrollToPage(i)
    }
  }

  HorizontalPager(
    state = pagerState,
    userScrollEnabled = false,
    beyondBoundsPageCount = 10, // Do not allow view release
  ) { page ->
    items[page].WebView()
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PullRefresh(modifier: Modifier, store: FrostWebStore, content: @Composable () -> Unit) {
  val refreshScope = rememberCoroutineScope()
  var refreshing by remember { mutableStateOf(false) }

  fun refresh() =
    refreshScope.launch {
      refreshing = true
      delay(1500)
      refreshing = false
    }

  val state = rememberPullRefreshState(refreshing, ::refresh)

  Box(modifier.pullRefresh(state)) {
    content()

    PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
  }
}
