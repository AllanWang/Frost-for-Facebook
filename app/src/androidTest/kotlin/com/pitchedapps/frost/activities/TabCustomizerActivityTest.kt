/*
 * Copyright 2021 Allan Wang
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

import com.pitchedapps.frost.helper.activityRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TabCustomizerActivityTest {

  @get:Rule(order = 0) val hildAndroidRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val activityRule = activityRule<TabCustomizerActivity>()

  @Test
  fun initializesSuccessfully() {
    activityRule.scenario.use {
      it.onActivity {
        // Verify no crash
      }
    }
  }
}
