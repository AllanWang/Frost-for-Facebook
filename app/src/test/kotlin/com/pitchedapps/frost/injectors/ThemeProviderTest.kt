/*
 * Copyright 2019 Allan Wang
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
package com.pitchedapps.frost.injectors

import com.pitchedapps.frost.enums.Theme
import com.pitchedapps.frost.enums.ThemeCategory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ThemeProviderTest {

  @Test
  fun verifyAssetsExist() {
    ThemeCategory.values().forEach { category ->
      Theme.values
        .filter { it != Theme.DEFAULT }
        .forEach { theme ->
          val file = File("src/web/assets/css/${category.folder}/themes/${theme.file}").absoluteFile
          assertTrue(file.exists(), "${theme.name} not found at ${file.path}")
        }
    }
  }
}
