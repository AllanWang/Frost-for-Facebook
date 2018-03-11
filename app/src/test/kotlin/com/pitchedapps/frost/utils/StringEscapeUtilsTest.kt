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