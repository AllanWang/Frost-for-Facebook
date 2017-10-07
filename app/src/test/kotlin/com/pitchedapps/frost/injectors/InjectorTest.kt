package com.pitchedapps.frost.injectors

import org.junit.Test

/**
 * Created by Allan Wang on 2017-10-06.
 *
 * Helper to print the injectors for external testing
 */
class InjectorTest {

    @Test
    fun printAll() {
        println("CSS Hider Injectors")
        CssHider.values().forEach {
            println("${it.injector.function}\n")
        }
    }
}
