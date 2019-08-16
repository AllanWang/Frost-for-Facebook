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

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class TagObfuscatorTest {

    /**
     * The same key should result in the same tag per session
     */
    @Test
    fun consistentTags() {
        val keys = generateSequence { UUID.randomUUID().toString() }.take(10).toSet()
        val tags = keys.map {
            val tag = generateSequence { TagObfuscator.obfuscateTag(it) }.take(10).toSet()
            assertEquals(1, tag.size, "Key $it produced multiple tags: $tag")
            tag.first()
        }
        assertEquals(keys.size, tags.size, "Key set and tag set have different sizes")
    }
}
