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
package com.pitchedapps.frost.dbflow

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

/**
 * Created by Allan Wang on 2017-05-30.
 */
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
