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