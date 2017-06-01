package com.pitchedapps.frost.dbflow

import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Database(name = CookiesDb.NAME, version = CookiesDb.VERSION)
object CookiesDb {
    const val NAME = "Cookies"
    const val VERSION = 1
}

@Table(database = CookiesDb::class, allFields = true, primaryKeyConflict = ConflictAction.REPLACE)
data class CookieModel(@PrimaryKey var id: Long = Prefs.userIdDefault, var cookie: String? = null) : BaseModel()

fun loadFbCookie(id: Long): CookieModel? = (select from CookieModel::class where (CookieModel_Table.id eq id)).querySingle()

fun saveFbCookie(id: Long, cookie: String?) {
    CookieModel(id, cookie).async save {
        L.d("Fb cookie saved")
    }
}

fun removeCookie(id: Long) {
    loadFbCookie(id)?.async?.delete({
        L.d("Fb cookie deleted")
    })
}