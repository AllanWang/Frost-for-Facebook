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
package com.pitchedapps.frost.internal

import com.pitchedapps.frost.facebook.FB_USER_MATCHER
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.utils.frostJsoup
import org.junit.Assume
import java.io.File
import java.io.FileInputStream
import java.util.Properties
import kotlin.reflect.full.starProjectedType
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Created by Allan Wang on 21/12/17.
 */

private const val FILE = "priv.properties"

private val propPaths = arrayOf(FILE, "../$FILE")

val PROPS: Properties by lazy {
    val props = Properties()
    val file = propPaths.map(::File).firstOrNull { it.isFile }
    if (file == null) {
        println("$FILE not found at ${File(".").absolutePath}")
        return@lazy props
    }
    println("Found properties at ${file.absolutePath}")
    FileInputStream(file).use { props.load(it) }
    props
}

val COOKIE: String by lazy { PROPS.getProperty("COOKIE") ?: "" }
val USER_ID: Long by lazy { FB_USER_MATCHER.find(COOKIE)[1]?.toLong() ?: -1 }

private val VALID_COOKIE: Boolean by lazy {
    val data = testJsoup(FbItem.SETTINGS.url)
    data.title() == "Settings"
}

fun testJsoup(url: String) = frostJsoup(COOKIE, url)

fun authDependent() {
    println("Auth Dependent")
    Assume.assumeTrue("Cookie cannot be empty", COOKIE.isNotEmpty())
    Assume.assumeTrue("Cookie is not valid", VALID_COOKIE)
}

/**
 * Check that component strings are nonempty and are properly parsed
 * To be used for data classes
 */
fun Any.assertComponentsNotEmpty() {
    val components = this::class.members.filter { it.name.startsWith("component") }
    if (components.isEmpty())
        fail("${this::class.simpleName} has no components")
    components.forEach {
        when (it.returnType) {
            String::class.starProjectedType -> {
                val result = it.call(this) as String
                assertTrue(result.isNotEmpty(), "${it.name} returned empty string")
                if (result.startsWith("https"))
                    assertTrue(result.startsWith("https://"), "${it.name} has poorly formatted output $result")
            }
        }
    }
}

fun <T : Comparable<T>> List<T>.assertDescending(tag: String) {
    assertEquals(sortedDescending(), this, "$tag not sorted in descending order")
}
