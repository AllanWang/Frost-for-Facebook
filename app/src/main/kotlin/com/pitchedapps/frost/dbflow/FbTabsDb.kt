package com.pitchedapps.frost.dbflow

import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.defaultTabs
import com.pitchedapps.frost.utils.L
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Created by Allan Wang on 2017-05-30.
 */

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
    val tabs: List<FbTabModel>? = (select from(FbTabModel::class)).orderBy(FbTabModel_Table.position, true).queryList()
    if (tabs?.isNotEmpty() == true) return tabs.map { it.tab }
    L.d("No tabs; loading default")
    return defaultTabs()
}

fun List<FbItem>.save() {
    mapIndexed { index, fbTab -> FbTabModel(index, fbTab) }.replace(FbTabsDb.NAME)
}