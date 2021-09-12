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

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 11/03/18.
 */
class StringEscapeUtilsTest {

    @Test
    fun utf() {
        val escaped = "\\u003Chead&gt; color=\\\"#3b5998\\\""
        assertEquals("<head> color=\"#3b5998\"", escaped.unescapeHtml())
    }
}
