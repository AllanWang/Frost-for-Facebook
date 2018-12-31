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
package com.pitchedapps.frost.debugger

import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.internal.COOKIE
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

/**
 * Created by Allan Wang on 05/01/18.
 */
class OfflineWebsiteTest {

    @Test
    fun fbOffline() {
        val buildPath = if (File("").absoluteFile.name == "app") "build/offline_test" else "app/build/offline_test"
        runBlocking {
            val success = OfflineWebsite(FB_URL_BASE, COOKIE, baseDir = File(buildPath))
                .loadAndZip("test") {println(it)}
            assertTrue(success, "An error occurred")
        }
    }
}
