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

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pitchedapps.frost.utils.L
import kotlinx.android.parcel.Parcelize

/**
 * Created by Allan Wang on 2017-05-30.
 */

/**
 * Generic cache to store serialized content
 */
@Entity(
    tableName = "frost_cache",
    primaryKeys = ["id", "type"],
    foreignKeys = [ForeignKey(
        entity = CookieEntity::class,
        parentColumns = ["cookie_id"],
        childColumns = ["id"],
        onDelete = ForeignKey.CASCADE
    )]
)
@Parcelize
data class CacheEntity(
    val id: Long,
    val type: String,
    val lastUpdated: Long,
    val contents: String
) : Parcelable

@Dao
interface CacheDao {

    @Query("SELECT * FROM frost_cache WHERE id = :id AND type = :type")
    fun _select(id: Long, type: String): CacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun _insertCache(cache: CacheEntity)

    @Query("DELETE FROM frost_cache WHERE id = :id AND type = :type")
    fun _delete(id: Long, type: String)
}

suspend fun CacheDao.select(id: Long, type: String) = dao {
    _select(id, type)
}

suspend fun CacheDao.delete(id: Long, type: String) = dao {
    _delete(id, type)
}

/**
 * Returns true if successful, given that there are constraints to the insertion
 */
suspend fun CacheDao.save(id: Long, type: String, contents: String): Boolean = dao {
    try {
        _insertCache(CacheEntity(id, type, System.currentTimeMillis(), contents))
        true
    } catch (e: Exception) {
        L.e(e) { "Cache save failed for $type" }
        false
    }
}
