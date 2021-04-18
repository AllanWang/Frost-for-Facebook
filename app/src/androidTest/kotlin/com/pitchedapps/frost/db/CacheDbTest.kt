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
import kotlin.test.assertTrue
import kotlin.test.fail

class CacheDbTest : BaseDbTest() {

    private val dao get() = db.cacheDao()
    private val cookieDao get() = db.cookieDao()

    private fun cookie(id: Long) = CookieEntity(id, "name$id", "cookie$id")

    @Test
    fun save() {
        val cookie = cookie(1L)
        val type = "test"
        val content = "long test".repeat(10000)
        runBlocking {
            cookieDao.save(cookie)
            dao.save(cookie.id, type, content)
            val cache = dao.select(cookie.id, type) ?: fail("Cache not found")
            assertEquals(content, cache.contents, "Content mismatch")
            assertTrue(
                System.currentTimeMillis() - cache.lastUpdated < 500,
                "Cache retrieval took over 500ms (${System.currentTimeMillis() - cache.lastUpdated})"
            )
        }
    }
}
