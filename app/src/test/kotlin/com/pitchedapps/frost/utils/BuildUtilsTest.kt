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
        assertEquals(BuildUtils.Data("1.0.0", ""), BuildUtils.match("1.0.0"))
        assertEquals(BuildUtils.Data("2.0.1", "26-af40533-debug"), BuildUtils.match("2.0.1-26-af40533-debug"))
    }

    @Test
    fun androidTests() {
        assertNotNull(BuildUtils.match(BuildConfig.VERSION_NAME))
    }
}