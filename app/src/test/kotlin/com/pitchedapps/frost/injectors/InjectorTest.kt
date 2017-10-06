package com.pitchedapps.frost.injectors

import org.junit.Test

/**
 * Created by Allan Wang on 2017-10-06.
 *
 * Helper to print the injectors for external testing
 */
class InjectorTest {

    private fun JsInjector.print()
            = println(function)

    @Test
    fun printAdHider() {
        CssHider.ADS.injector.print()
    }
}
