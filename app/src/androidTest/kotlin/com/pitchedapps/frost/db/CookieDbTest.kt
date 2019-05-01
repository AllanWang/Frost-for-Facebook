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

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CookieDbTest : BaseDbTest() {

    private val dao get() = db.cookieDao()

    @Test
    fun basicCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")
        runBlocking {
            dao.save(cookie)
            val cookies = dao.selectAll()
            assertEquals(listOf(cookie), cookies, "Cookie mismatch")
        }
    }

    @Test
    fun deleteCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")

        runBlocking {
            dao.save(cookie)
            dao.deleteById(cookie.id + 1)
            assertEquals(
                listOf(cookie),
                dao.selectAll(),
                "Cookie list should be the same after inexistent deletion"
            )
            dao.deleteById(cookie.id)
            assertEquals(emptyList(), dao.selectAll(), "Cookie list should be empty after deletion")
        }
    }

    @Test
    fun insertReplaceCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")
        runBlocking {
            dao.save(cookie)
            assertEquals(listOf(cookie), dao.selectAll(), "Cookie insertion failed")
            dao.save(cookie.copy(name = "testName2"))
            assertEquals(
                listOf(cookie.copy(name = "testName2")),
                dao.selectAll(),
                "Cookie replacement failed"
            )
            dao.save(cookie.copy(id = 123L))
            assertEquals(
                setOf(cookie.copy(id = 123L), cookie.copy(name = "testName2")),
                dao.selectAll().toSet(),
                "New cookie insertion failed"
            )
        }
    }

    @Test
    fun selectCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")
        runBlocking {
            dao.save(cookie)
            assertEquals(cookie, dao.selectById(cookie.id), "Cookie selection failed")
            assertNull(dao.selectById(cookie.id + 1), "Inexistent cookie selection failed")
        }
    }
}
