package com.pitchedapps.frost.db

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CookieDbTest : BaseDbTest() {

    @Test
    fun basicCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")
        runBlocking {
            db.cookieDao().insertCookie(cookie)
            val cookies = db.cookieDao().selectAll()
            assertEquals(listOf(cookie), cookies, "Cookie mismatch")
        }
    }

    @Test
    fun deleteCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")

        runBlocking {
            db.cookieDao().insertCookie(cookie)
            db.cookieDao().deleteById(cookie.id + 1)
            assertEquals(
                listOf(cookie),
                db.cookieDao().selectAll(),
                "Cookie list should be the same after inexistent deletion"
            )
            db.cookieDao().deleteById(cookie.id)
            assertEquals(emptyList(), db.cookieDao().selectAll(), "Cookie list should be empty after deletion")
        }
    }

    @Test
    fun insertReplaceCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")
        runBlocking {
            db.cookieDao().insertCookie(cookie)
            assertEquals(listOf(cookie), db.cookieDao().selectAll(), "Cookie insertion failed")
            db.cookieDao().insertCookie(cookie.copy(name = "testName2"))
            assertEquals(
                listOf(cookie.copy(name = "testName2")),
                db.cookieDao().selectAll(),
                "Cookie replacement failed"
            )
            db.cookieDao().insertCookie(cookie.copy(id = 123L))
            assertEquals(
                setOf(cookie.copy(id = 123L), cookie.copy(name = "testName2")),
                db.cookieDao().selectAll().toSet(),
                "New cookie insertion failed"
            )
        }
    }

    @Test
    fun selectCookie() {
        val cookie = CookieEntity(id = 1234L, name = "testName", cookie = "testCookie")
        runBlocking {
            db.cookieDao().insertCookie(cookie)
            assertEquals(cookie, db.cookieDao().selectById(cookie.id), "Cookie selection failed")
            assertNull(db.cookieDao().selectById(cookie.id + 1), "Inexistent cookie selection failed")
        }
    }
}