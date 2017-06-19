package com.pitchedapps.frost.dbflow

import android.os.Parcel
import android.os.Parcelable
import com.pitchedapps.frost.utils.L
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.structure.BaseModel
import paperparcel.PaperParcel

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Database(name = CookiesDb.NAME, version = CookiesDb.VERSION)
object CookiesDb {
    const val NAME = "Cookies"
    const val VERSION = 2
}

@PaperParcel
@Table(database = CookiesDb::class, allFields = true, primaryKeyConflict = ConflictAction.REPLACE)
data class CookieModel(@PrimaryKey var id: Long = -1L, var name: String? = null, var cookie: String? = null) : BaseModel(), Parcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelCookieModel.CREATOR
    }

    override fun describeContents() = 0
    override fun writeToParcel(dest: Parcel, flags: Int) = PaperParcelCookieModel.writeToParcel(this, dest, flags)
}

fun loadFbCookie(id: Long): CookieModel? = (select from CookieModel::class where (CookieModel_Table.id eq id)).querySingle()
fun loadFbCookie(name: String): CookieModel? = (select from CookieModel::class where (CookieModel_Table.name eq name)).querySingle()

/**
 * Loads cookies sorted by name
 */
fun loadFbCookiesAsync(callback: (cookies: List<CookieModel>) -> Unit) {
    (select from CookieModel::class).orderBy(CookieModel_Table.name, true).async().queryListResultCallback { _, tResult -> callback.invoke(tResult) }.execute()
}

fun loadFbCookiesSync(): List<CookieModel> = (select from CookieModel::class).orderBy(CookieModel_Table.name, true).queryList()


fun saveFbCookie(cookie: CookieModel, callback: (() -> Unit)? = null) {
    cookie.async save {
        L.d("Fb cookie $cookie saved")
        callback?.invoke()
    }
}

fun removeCookie(id: Long) {
    loadFbCookie(id)?.async?.delete({
        L.d("Fb cookie $id deleted")
    })
}