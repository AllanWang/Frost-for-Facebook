/*
 * Copyright 2018 Allan Wang
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
package com.pitchedapps.frost.activities

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.parsers.BadgeParser
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.views.BadgedIcon
import com.pitchedapps.frost.web.FrostEmitter
import com.pitchedapps.frost.web.asFrostEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

class MainActivity : BaseMainActivity() {

  private val fragmentMutableFlow =
    MutableSharedFlow<Int>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  override val fragmentFlow: SharedFlow<Int> = fragmentMutableFlow.asSharedFlow()
  override val fragmentEmit: FrostEmitter<Int> = fragmentMutableFlow.asFrostEmitter()

  private val headerMutableFlow = MutableStateFlow("")
  override val headerFlow: SharedFlow<String> = headerMutableFlow.asSharedFlow()
  override val headerEmit: FrostEmitter<String> = headerMutableFlow.asFrostEmitter()

  override fun onNestedCreate(savedInstanceState: Bundle?) {
    with(contentBinding) {
      setupTabs()
      setupViewPager()
    }
  }

  private fun ActivityMainContentBinding.setupViewPager() {
    viewpager.addOnPageChangeListener(
      object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
          super.onPageSelected(position)
          if (lastPosition == position) {
            return
          }
          if (lastPosition != -1) {
            fragmentEmit(-(lastPosition + 1))
          }
          fragmentEmit(position)
          lastPosition = position
        }

        override fun onPageScrolled(
          position: Int,
          positionOffset: Float,
          positionOffsetPixels: Int
        ) {
          super.onPageScrolled(position, positionOffset, positionOffsetPixels)
          val delta = positionOffset * (SELECTED_TAB_ALPHA - UNSELECTED_TAB_ALPHA)
          tabsForEachView { tabPosition, view ->
            view.setAllAlpha(
              when (tabPosition) {
                position -> SELECTED_TAB_ALPHA - delta
                position + 1 -> UNSELECTED_TAB_ALPHA + delta
                else -> UNSELECTED_TAB_ALPHA
              }
            )
          }
        }
      }
    )
  }

  private fun ActivityMainContentBinding.setupTabs() {
    viewpager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
    tabs.addOnTabSelectedListener(
      object : TabLayout.ViewPagerOnTabSelectedListener(viewpager) {
        override fun onTabReselected(tab: TabLayout.Tab) {
          super.onTabReselected(tab)
          currentFragment?.onTabClick()
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
          super.onTabSelected(tab)
          (tab.customView as BadgedIcon).badgeText = null
        }
      }
    )
    headerFlow
      .filter { it.isNotBlank() }
      .mapNotNull { html ->
        BadgeParser.parseFromData(cookie = fbCookie.webCookie, text = html)?.data
      }
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
      .onEach { data ->
        L.v { "Badges $data" }
        tabsForEachView { _, view ->
          when (view.iicon) {
            FbItem.FEED.icon -> view.badgeText = data.feed
            FbItem.FRIENDS.icon -> view.badgeText = data.friends
            FbItem.MESSAGES.icon -> view.badgeText = data.messages
            FbItem.NOTIFICATIONS.icon -> view.badgeText = data.notifications
          }
        }
      }
      .flowOn(Dispatchers.Main)
      .launchIn(this@MainActivity)
  }
}
