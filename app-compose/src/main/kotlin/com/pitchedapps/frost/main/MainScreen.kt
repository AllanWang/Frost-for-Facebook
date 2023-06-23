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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pitchedapps.frost.compose.FrostTheme
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.tab
import com.pitchedapps.frost.tabselector.TabData
import kotlinx.coroutines.launch

@Composable
fun MainScreenContainer(
  modifier: Modifier = Modifier,
  drawerItems: List<TabData>,
  drawerSelectedIndex: Int,
  drawerOnSelect: (Int) -> Unit,
  navItems: List<TabData>,
  navSelectedIndex: Int,
  navOnSelect: (Int) -> Unit,
  content: @Composable (PaddingValues) -> Unit,
) {

  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  ModalNavigationDrawer(
    modifier = modifier,
    drawerState = drawerState,
    drawerContent = {
      MainModalDrawerContent(
        items = drawerItems,
        selectedIndex = drawerSelectedIndex,
        onSelect = {
          scope.launch { drawerState.close() }
          drawerOnSelect(it)
        },
      )
    },
    //    drawerShape = …,
    //  drawerTonalElevation = …,
    //  drawerContainerColor = …,
    //  drawerContentColor = …,
  ) {
    Scaffold(
      containerColor = Color.Transparent,
      topBar = { MainTopBar() },
      bottomBar = {
        MainBottomBar(
          selectedIndex = navSelectedIndex,
          items = navItems,
          onSelect = { navOnSelect(it) },
        )
      },
      content = content,
    )
  }
}

@Composable
private fun MainModalDrawerContent(
  modifier: Modifier = Modifier,
  items: List<TabData>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
) {
  ModalDrawerSheet(modifier = modifier) {
    LazyColumn {
      items(items.size) { i ->
        val item = items[i]
        NavigationDrawerItem(
          icon = {
            Icon(
              modifier = Modifier.size(24.dp),
              imageVector = item.icon,
              contentDescription = null,
            )
          },
          label = { Text(item.title) },
          selected = i == selectedIndex,
          onClick = { onSelect(i) },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(modifier: Modifier = Modifier) {
  TopAppBar(modifier = modifier, title = { Text(text = "Title") })
}

@Composable
fun MainBottomBar(
  modifier: Modifier = Modifier,
  selectedIndex: Int,
  items: List<TabData>,
  onSelect: (Int) -> Unit
) {
  NavigationBar(
    modifier = modifier,
  ) {
    for ((i, item) in items.withIndex()) {
      NavigationBarItem(
        icon = {
          Icon(
            modifier = Modifier.size(24.dp),
            imageVector = item.icon,
            contentDescription = item.title,
          )
        },
        selected = i == selectedIndex,
        onClick = { onSelect(i) },
      )
    }
  }
}

@Composable
@Preview
fun MainScreenContainerPreview() {
  val context = LocalContext.current

  val drawerItems = remember { FbItem.values().map { it.tab(context) } }

  var drawerSelectedIndex by remember { mutableIntStateOf(0) }

  val navItems = remember { listOf(FbItem.Feed, FbItem.Messages).map { it.tab(context) } }

  var navSelectedIndex by remember { mutableIntStateOf(0) }

  FrostTheme {
    MainScreenContainer(
      drawerItems = drawerItems,
      drawerSelectedIndex = drawerSelectedIndex,
      drawerOnSelect = { drawerSelectedIndex = it },
      navItems = navItems,
      navSelectedIndex = navSelectedIndex,
      navOnSelect = { navSelectedIndex = it },
    ) {}
  }
}
