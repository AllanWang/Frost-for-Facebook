package com.pitchedapps.frost.dbflow

import android.content.Context
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.defaultTabs
import com.pitchedapps.frost.utils.L
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Database(name = FbTabsDb.NAME, version = FbTabsDb.VERSION)
object FbTabsDb {
    const val NAME = "FrostTabs"
    const val VERSION = 1
}

@Table(database = FbTabsDb::class, allFields = true)
data class FbTabModel(@PrimaryKey var position: Int = -1, var tab: FbTab = FbTab.FEED) : BaseModel()

//const val FB_URL_BASE = "https://touch.facebook.com/"

//BOOKMARKS("https://touch.facebook.com/bookmarks"),
//SEARCH("https://touch.facebook.com/search"),

fun loadFbTabs(): List<FbTab> {
    val tabs: List<FbTabModel>? = SQLite.select().from(FbTabModel::class).orderBy(FbTabModel_Table.position, true).queryList()
    if (tabs?.isNotEmpty() ?: false) return tabs!!.map { it.tab }
    L.d("No tabs; loading default")
    return defaultTabs()
}

fun List<FbTab>.saveAsync(c: Context) {
    mapIndexed { index, fbTab -> FbTabModel(index, fbTab) }.replace(c, FbTabsDb.NAME)
}