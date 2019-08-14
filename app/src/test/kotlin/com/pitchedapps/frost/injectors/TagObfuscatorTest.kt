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
