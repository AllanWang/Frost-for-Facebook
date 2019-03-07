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
            dao.insertCookie(cookie)
            val cookies = dao.selectAll()
            assertEquals(listOf(cookie), cookies, "Cookie mismatch")
        }
    }

    @Test
    fun deleteCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")

        runBlocking {
            dao.insertCookie(cookie)
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
            dao.insertCookie(cookie)
            assertEquals(listOf(cookie), dao.selectAll(), "Cookie insertion failed")
            dao.insertCookie(cookie.copy(name = "testName2"))
            assertEquals(
                listOf(cookie.copy(name = "testName2")),
                dao.selectAll(),
                "Cookie replacement failed"
            )
            dao.insertCookie(cookie.copy(id = 123L))
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
            dao.insertCookie(cookie)
            assertEquals(cookie, dao.selectById(cookie.id), "Cookie selection failed")
            assertNull(dao.selectById(cookie.id + 1), "Inexistent cookie selection failed")
        }
    }
}