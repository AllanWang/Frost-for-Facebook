package com.pitchedapps.frost

import com.pitchedapps.frost.injectors.CssHider
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Allan Wang on 2017-06-14.
 */
class MiscTest {

    @Test
    fun headerFunction() {
        print(CssHider.HEADER.injector.function)
    }

    @Test
    fun nullPair() {
        assertEquals(Pair<String?, Int>(null, 2), Pair<String?, Int>(null, 2))
    }
}