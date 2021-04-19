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
package com.pitchedapps.frost.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.koin.core.error.NoBeanDefFoundException
import org.koin.test.KoinTest
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DatabaseTest : KoinTest {

    inline fun <reified T : Any> hasKoin() = hasKoin(T::class)

    fun <T : Any> hasKoin(klazz: KClass<T>): Boolean =
        try {
            getKoin().get<T>(klazz, qualifier = null, parameters = null)
            true
        } catch (e: NoBeanDefFoundException) {
            false
        }

    /**
     * Database and all daos should be loaded as components
     */
    @Test
    fun testKoins() {
        hasKoin<FrostDatabase>()
        val members = FrostDatabase::class.java.kotlin.functions.filter { it.name.endsWith("Dao") }
            .mapNotNull { it.returnType.classifier as? KClass<*> }
        assertTrue(members.isNotEmpty(), "Failed to find dao interfaces")
        val missingKoins = members.filter { !hasKoin(it) }
        assertTrue(missingKoins.isEmpty(), "Missing koins: $missingKoins")
    }
}
