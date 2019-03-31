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
package com.pitchedapps.frost.utils

import com.pitchedapps.frost.BuildConfig
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BuildUtilsTest {

    @Test
    fun matchingVersions() {
        assertNull(BuildUtils.match("unknown"))
        assertEquals(BuildUtils.Data("v1.0.0", ""), BuildUtils.match("1.0.0"))
        assertEquals(BuildUtils.Data("v2.0.1", "26-af40533-debug"), BuildUtils.match("2.0.1-26-af40533-debug"))
    }

    @Test
    fun androidTests() {
        assertNotNull(BuildUtils.match(BuildConfig.VERSION_NAME))
    }
}
