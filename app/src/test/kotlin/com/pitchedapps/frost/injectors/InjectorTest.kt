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
