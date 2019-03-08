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
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.defaultTabs

/**
 * Created by Allan Wang on 2017-05-30.
 */

/**
 * Generic cache to store serialized content
 */
@Entity(tableName = "frost_generic")
data class GenericEntity(
    @PrimaryKey
    val type: String,
    val contents: String
)

@Dao
interface GenericDao {

    @Query("SELECT contents FROM frost_generic WHERE type = :type")
    suspend fun select(type: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: GenericEntity)

    @Query("DELETE FROM frost_generic WHERE type = :type")
    suspend fun delete(type: String)

    companion object {
        const val TYPE_TABS = "generic_tabs"
    }
}

suspend fun GenericDao.saveTabs(tabs: List<FbItem>) {
    val content = tabs.joinToString(",") { it.name }
    save(GenericEntity(GenericDao.TYPE_TABS, content))
}

suspend fun GenericDao.getTabs(): List<FbItem> {
    val allTabs = FbItem.values.map { it.name to it }.toMap()
    return select(GenericDao.TYPE_TABS)
        ?.split(",")
        ?.mapNotNull { allTabs[it] }
        ?.takeIf { it.isNotEmpty() }
        ?: defaultTabs()
}
