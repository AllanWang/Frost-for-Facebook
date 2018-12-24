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

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 2017-05-31.
 */
class PrefsTest {

    //Replicate logic
    var test: Long = -1L
        get() {
            if (field == -1L) field = file
            return field
        }
        set(value) {
            field = value
            if (value != -1L) file = value
        }

    var file: Long = -1L

    @Before
    fun verify() {
        test = -1L
        file = -1L
    }

    @Test
    fun laziness() {
        assertEquals(-1L, test)
        file = 2L
        assertEquals(2L, test)
        file = -3L
        assertEquals(2L, test)
        test = 3L
        assertEquals(3L, file)
    }
}
