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
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pitchedapps.frost.utils.Prefs
import kotlinx.android.parcel.Parcelize

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Entity(tableName = "cookies")
@Parcelize
data class CookieEntity(
    @androidx.room.PrimaryKey
    @ColumnInfo(name = "cookie_id")
    val id: Long,
    val name: String?,
    val cookie: String?
) : Parcelable {
    override fun toString(): String = "CookieEntity(${hashCode()})"

    fun toSensitiveString(): String = "CookieEntity(id=$id, name=$name, cookie=$cookie)"
}

@Dao
interface CookieDao {

    @Query("SELECT * FROM cookies")
    fun _selectAll(): List<CookieEntity>

    @Query("SELECT * FROM cookies WHERE cookie_id = :id")
    fun _selectById(id: Long): CookieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun _save(cookie: CookieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun _save(cookies: List<CookieEntity>)

    @Query("DELETE FROM cookies WHERE cookie_id = :id")
    fun _deleteById(id: Long)
}

suspend fun CookieDao.selectAll() = dao { _selectAll() }
suspend fun CookieDao.selectById(id: Long) = dao { _selectById(id) }
suspend fun CookieDao.save(cookie: CookieEntity) = dao { _save(cookie) }
suspend fun CookieDao.save(cookies: List<CookieEntity>) = dao { _save(cookies) }
suspend fun CookieDao.deleteById(id: Long) = dao { _deleteById(id) }
suspend fun CookieDao.currentCookie() = selectById(Prefs.userId)
