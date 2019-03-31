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
package com.pitchedapps.frost.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pitchedapps.frost.db.CookieModel_Table.cookie
import com.pitchedapps.frost.facebook.parsers.FrostThread
import com.pitchedapps.frost.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Entity(
    tableName = "threads",
    primaryKeys = ["thread_id", "userId"],
    foreignKeys = [ForeignKey(
        entity = CookieEntity::class,
        parentColumns = ["cookie_id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("thread_id"), Index("userId")]
)
data class ThreadEntity(
    @Embedded(prefix = "thread_")
    val thread: FrostThread,
    val userId: Long
)

data class ThreadContentEntity(
    @Embedded
    val cookie: CookieEntity,
    @Embedded(prefix = "thread_")
    val thread: FrostThread
)

@Dao
interface ThreadDao {

    /**
     * Note that notifications are guaranteed to be ordered by descending timestamp
     */
    @Transaction
    @Query("SELECT * FROM cookies INNER JOIN threads ON cookie_id = userId WHERE userId = :userId  ORDER BY thread_time DESC")
    fun _selectThreads(userId: Long): List<ThreadContentEntity>

    @Query("SELECT thread_time FROM threads WHERE userId = :userId ORDER BY thread_time DESC LIMIT 1")
    fun _selectEpoch(userId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun _insertThreads(notifs: List<ThreadEntity>)

    @Query("DELETE FROM threads WHERE userId = :userId ")
    fun _deleteThreads(userId: Long)

    @Query("DELETE FROM threads")
    suspend fun deleteAll()

    /**
     * It is assumed that the notification batch comes from the same user
     */
    @Transaction
    fun _saveThreads(userId: Long, notifs: List<FrostThread>) {
        val entities = notifs.map { ThreadEntity(it, userId) }
        _deleteThreads(userId)
        _insertThreads(entities)
    }
}

suspend fun ThreadDao.selectThreads(userId: Long): List<ThreadContentEntity> =
    withContext(Dispatchers.IO) {
        _selectThreads(userId)
    }

/**
 * Returns true if successful, given that there are constraints to the insertion
 */
suspend fun ThreadDao.saveThreads(userId: Long, threads: List<FrostThread>): Boolean {
    if (threads.isEmpty()) return true
    return withContext(Dispatchers.IO) {
        try {
            _saveThreads(userId, threads)
            true
        } catch (e: Exception) {
            L.e(e) { "Thread save failed" }
            false
        }
    }
}

suspend fun ThreadDao.latestEpoch(userId: Long, type: String): Long =
    withContext(Dispatchers.IO) {
        _selectEpoch(userId) ?: lastNotificationTime(userId).epochIm
    }
