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