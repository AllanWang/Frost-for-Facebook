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