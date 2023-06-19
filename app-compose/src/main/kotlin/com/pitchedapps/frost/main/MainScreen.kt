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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pitchedapps.frost.compose.FrostCoreExtensionEffect
import com.pitchedapps.frost.compose.FrostWeb
import com.pitchedapps.frost.ext.GeckoContextId
import com.pitchedapps.frost.ext.components
import mozilla.components.browser.state.helper.Target

@Composable
fun MainScreen(modifier: Modifier = Modifier, tabs: List<MainTabItem>) {
  val vm: MainScreenViewModel = viewModel()

  if (tabs.isEmpty()) return // not ready

  val contextId = vm.contextIdFlow.collectAsState(initial = null).value ?: return // not ready

  FrostCoreExtensionEffect()

  val onTabSelect =
    remember(vm) {
      { selectedIndex: Int ->
        if (selectedIndex == vm.tabIndex) {
          vm.components.useCases.homeTabs.reloadTab(selectedIndex)
          //          context.launchFloatingUrl(FACEBOOK_M_URL)
        } else {
          // Change? What if previous selected tab is not home tab
          vm.components.useCases.homeTabs.selectHomeTab(selectedIndex)
          vm.tabIndex = selectedIndex
        }
      }
    }

  MainContainer(
    modifier = modifier,
    contextId = contextId,
    tabIndex = vm.tabIndex,
    tabs = tabs,
    onTabSelect = onTabSelect,
  )
}

@Composable
private fun MainContainer(
  contextId: GeckoContextId,
  tabIndex: Int,
  tabs: List<MainTabItem>,
  onTabSelect: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val components = LocalContext.current.components

  LaunchedEffect(contextId, tabs) {
    components.useCases.homeTabs.createHomeTabs(contextId, tabIndex, tabs.map { it.url })
  }

  Column(modifier = modifier) {
    if (tabs.size > 1) {
      MainTabRow(
        modifier = Modifier.statusBarsPadding(),
        selectedIndex = tabIndex,
        items = tabs,
        onTabSelect = onTabSelect,
      )
    }
    // For tab switching, must use SelectedTab
    // https://github.com/mozilla-mobile/android-components/issues/12798
    FrostWeb(
      engine = components.core.engine,
      store = components.core.store,
      target = Target.SelectedTab,
    )
  }
}

@Composable
fun MainTabRow(
  selectedIndex: Int,
  items: List<MainTabItem>,
  onTabSelect: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  TabRow(modifier = modifier, selectedTabIndex = selectedIndex, indicator = {}) {
    items.forEachIndexed { i, item ->
      val selected = selectedIndex == i
      Tab(selected = selected, onClick = { onTabSelect(i) }) {
        MainTabItem(modifier = Modifier.padding(16.dp), item = item, selected = selected)
      }
    }
  }
}

@Composable
private fun MainTabItem(item: MainTabItem, selected: Boolean, modifier: Modifier = Modifier) {
  val alpha by
    animateFloatAsState(
      targetValue = if (selected) 1f else ContentAlpha.medium,
      label = "Tab Alpha",
    )
  Icon(
    modifier = modifier.alpha(alpha).size(24.dp),
    contentDescription = item.title,
    imageVector = item.icon,
  )
}
