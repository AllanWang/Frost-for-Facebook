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
package com.pitchedapps.frost.facebook.requests

import com.pitchedapps.frost.internal.COOKIE
import com.pitchedapps.frost.internal.authDependent
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertNotNull

/**
 * Created by Allan Wang on 12/04/18.
 */
class FbFullImageTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun before() {
            authDependent()
        }
    }

    @Test
    fun getFullImage() {
        val url = "https://touch.facebook.com/photo/view_full_size/?fbid=107368839645039"
        val result = runBlocking {
            COOKIE.getFullSizedImageUrl(url)
        }
        assertNotNull(result)
        println(result)
    }
}
