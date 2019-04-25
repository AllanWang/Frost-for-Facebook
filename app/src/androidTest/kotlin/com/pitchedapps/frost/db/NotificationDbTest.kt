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

import com.pitchedapps.frost.services.NOTIF_CHANNEL_GENERAL
import com.pitchedapps.frost.services.NOTIF_CHANNEL_MESSAGES
import com.pitchedapps.frost.services.NotificationContent
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationDbTest : BaseDbTest() {

    private val dao get() = db.notifDao()

    private fun cookie(id: Long) = CookieEntity(id, "name$id", "cookie$id")

    private fun notifContent(id: Long, cookie: CookieEntity, time: Long = id) = NotificationContent(
        data = cookie,
        id = id,
        href = "",
        title = null,
        text = "",
        timestamp = time,
        profileUrl = null,
        unread = true
    )

    @Test
    fun saveAndRetrieve() {
        val cookie = cookie(12345L)
        // Unique unsorted ids
        val notifs = listOf(0L, 4L, 2L, 6L, 99L, 3L).map { notifContent(it, cookie) }
        runBlocking {
            db.cookieDao().save(cookie)
            dao.saveNotifications(NOTIF_CHANNEL_GENERAL, notifs)
            val dbNotifs = dao.selectNotifications(cookie.id, NOTIF_CHANNEL_GENERAL)
            assertEquals(notifs.sortedByDescending { it.timestamp }, dbNotifs, "Incorrect notification list received")
        }
    }

    @Test
    fun selectConditions() {
        runBlocking {
            val cookie1 = cookie(12345L)
            val cookie2 = cookie(12L)
            val notifs1 = (0L..2L).map { notifContent(it, cookie1) }
            val notifs2 = (5L..10L).map { notifContent(it, cookie2) }
            db.cookieDao().save(cookie1)
            db.cookieDao().save(cookie2)
            dao.saveNotifications(NOTIF_CHANNEL_GENERAL, notifs1)
            dao.saveNotifications(NOTIF_CHANNEL_MESSAGES, notifs2)
            assertEquals(
                emptyList(),
                dao.selectNotifications(cookie1.id, NOTIF_CHANNEL_MESSAGES),
                "Filtering by type did not work for cookie1"
            )
            assertEquals(
                notifs1.sortedByDescending { it.timestamp },
                dao.selectNotifications(cookie1.id, NOTIF_CHANNEL_GENERAL),
                "Selection for cookie1 failed"
            )
            assertEquals(
                emptyList(),
                dao.selectNotifications(cookie2.id, NOTIF_CHANNEL_GENERAL),
                "Filtering by type did not work for cookie2"
            )
            assertEquals(
                notifs2.sortedByDescending { it.timestamp },
                dao.selectNotifications(cookie2.id, NOTIF_CHANNEL_MESSAGES),
                "Selection for cookie2 failed"
            )
        }
    }

    /**
     * Primary key is both id and userId, in the event that the same notification to multiple users has the same id
     */
    @Test
    fun primaryKeyCheck() {
        runBlocking {
            val cookie1 = cookie(12345L)
            val cookie2 = cookie(12L)
            val notifs1 = (0L..2L).map { notifContent(it, cookie1) }
            val notifs2 = notifs1.map { it.copy(data = cookie2) }
            db.cookieDao().save(cookie1)
            db.cookieDao().save(cookie2)
            assertTrue(dao.saveNotifications(NOTIF_CHANNEL_GENERAL, notifs1), "Notif1 save failed")
            assertTrue(dao.saveNotifications(NOTIF_CHANNEL_GENERAL, notifs2), "Notif2 save failed")
        }
    }

    @Test
    fun cascadeDeletion() {
        val cookie = cookie(12345L)
        // Unique unsorted ids
        val notifs = listOf(0L, 4L, 2L, 6L, 99L, 3L).map { notifContent(it, cookie) }
        runBlocking {
            db.cookieDao().save(cookie)
            dao.saveNotifications(NOTIF_CHANNEL_GENERAL, notifs)
            db.cookieDao().deleteById(cookie.id)
            val dbNotifs = dao.selectNotifications(cookie.id, NOTIF_CHANNEL_GENERAL)
            assertTrue(dbNotifs.isEmpty(), "Cascade deletion failed")
        }
    }

    @Test
    fun latestEpoch() {
        val cookie = cookie(12345L)
        // Unique unsorted ids
        val notifs = listOf(0L, 4L, 2L, 6L, 99L, 3L).map { notifContent(it, cookie) }
        runBlocking {
            assertEquals(-1L, dao.latestEpoch(cookie.id, NOTIF_CHANNEL_GENERAL), "Default epoch failed")
            db.cookieDao().save(cookie)
            dao.saveNotifications(NOTIF_CHANNEL_GENERAL, notifs)
            assertEquals(99L, dao.latestEpoch(cookie.id, NOTIF_CHANNEL_GENERAL), "Latest epoch failed")
        }
    }

    @Test
    fun insertionWithInvalidCookies() {
        runBlocking {
            assertFalse(
                dao.saveNotifications(NOTIF_CHANNEL_GENERAL, listOf(notifContent(1L, cookie(2L)))),
                "Notif save should not have passed without relevant cookie entries"
            )
        }
    }
}
