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
package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.internal.authDependent
import com.pitchedapps.frost.internal.testJsoup
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertNotNull

class FbDomTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun before() {
            authDependent()
        }
    }

    @Test
    fun checkHeaders() {
        val doc = testJsoup(FB_URL_BASE)
        assertNotNull(doc.getElementById("header"))
        assertNotNull(doc.getElementById("mJewelNav"))
    }
}
