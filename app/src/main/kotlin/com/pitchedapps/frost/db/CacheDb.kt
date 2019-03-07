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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.android.parcel.Parcelize

/**
 * Created by Allan Wang on 2017-05-30.
 */

/**
 * Generic cache to store serialized content
 */
@Entity(tableName = "frost_cache")
@Parcelize
data class CacheEntity(
    @androidx.room.PrimaryKey
    val id: String,
    val lastUpdated: Long,
    val contents: String
) : Parcelable

@Dao
interface CacheDao {

    @Query("SELECT * FROM frost_cache WHERE id = :id")
    suspend fun selectById(id: Long): CacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: CacheEntity)

    @Query("DELETE FROM frost_cache WHERE id = :id")
    suspend fun deleteById(id: Long)
}

suspend fun CacheDao.save(id: String, contents: String) =
    insertCache(CacheEntity(id, System.currentTimeMillis(), contents))