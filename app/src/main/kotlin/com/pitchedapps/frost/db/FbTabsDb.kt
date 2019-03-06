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
import androidx.room.Query
import androidx.room.Transaction
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.defaultTabs
import com.pitchedapps.frost.utils.L
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.database
import com.raizlabs.android.dbflow.kotlinextensions.fastSave
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.structure.BaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Entity(tableName = "tabs")
data class FbTabEntity(@androidx.room.PrimaryKey val position: Int, val tab: FbItem)

@Dao
interface FbTabDao {

    @Query("SELECT * FROM tabs ORDER BY position ASC")
    fun _selectAll(): List<FbTabEntity>

    @Query("DELETE FROM tabs")
    fun _deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun _insertAll(items: List<FbTabEntity>)

    @Transaction
    fun _save(items: List<FbTabEntity>) {
        _deleteAll()
        _insertAll(items)
    }
}

/**
 * Saving tabs operates by deleting all db items and saving the new list.
 * Transactions can't be done with suspensions in room as switching threads during the process
 * may result in a deadlock.
 * That's why we disallow thread switching within the transaction, but wrap the entire thing in a coroutine
 */
suspend fun FbTabDao.save(items: List<FbItem>) {
    withContext(Dispatchers.IO) {
        val entities = (items.takeIf { it.isNotEmpty() } ?: defaultTabs()).mapIndexed { index, fbItem ->
            FbTabEntity(
                index,
                fbItem
            )
        }
        _save(entities)
    }
}

suspend fun FbTabDao.selectAll(): List<FbItem> = withContext(Dispatchers.IO) {
    _selectAll().map { it.tab }.takeIf { it.isNotEmpty() } ?: defaultTabs()
}

object FbItemConverter {
    @androidx.room.TypeConverter
    @JvmStatic
    fun fromItem(item: FbItem): String = item.name

    @androidx.room.TypeConverter
    @JvmStatic
    fun toItem(value: String): FbItem = FbItem.valueOf(value)
}

const val TAB_COUNT = 4

@Database(version = FbTabsDb.VERSION)
object FbTabsDb {
    const val NAME = "FrostTabs"
    const val VERSION = 1
}

@Table(database = FbTabsDb::class, allFields = true)
data class FbTabModel(@PrimaryKey var position: Int = -1, var tab: FbItem = FbItem.FEED) : BaseModel()

/**
 * Load tabs synchronously
 * Note that tab length should never be a big number anyways
 */
fun loadFbTabs(): List<FbItem> {
    val tabs: List<FbTabModel>? = (select from (FbTabModel::class)).orderBy(FbTabModel_Table.position, true).queryList()
    if (tabs?.size == TAB_COUNT) return tabs.map(FbTabModel::tab)
    L.d { "No tabs (${tabs?.size}); loading default" }
    return defaultTabs()
}

fun List<FbItem>.save() {
    database<FbTabsDb>().beginTransactionAsync(mapIndexed(::FbTabModel).fastSave().build()).execute()
}
