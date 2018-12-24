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

import android.os.Parcel
import android.os.Parcelable
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.utils.logFrostEvent
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.async
import com.raizlabs.android.dbflow.kotlinextensions.delete
import com.raizlabs.android.dbflow.kotlinextensions.eq
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.structure.BaseModel
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import paperparcel.PaperParcel
import java.net.UnknownHostException

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Database(version = CookiesDb.VERSION)
object CookiesDb {
    const val NAME = "Cookies"
    const val VERSION = 2
}

@PaperParcel
@Table(database = CookiesDb::class, allFields = true, primaryKeyConflict = ConflictAction.REPLACE)
data class CookieModel(@PrimaryKey var id: Long = -1L, var name: String? = null, var cookie: String? = null) :
    BaseModel(), Parcelable {
    companion object {
        @JvmField
        val CREATOR = PaperParcelCookieModel.CREATOR
    }

    override fun describeContents() = 0
    override fun writeToParcel(dest: Parcel, flags: Int) = PaperParcelCookieModel.writeToParcel(this, dest, flags)
}

fun loadFbCookie(id: Long): CookieModel? =
    (select from CookieModel::class where (CookieModel_Table.id eq id)).querySingle()

fun loadFbCookie(name: String): CookieModel? =
    (select from CookieModel::class where (CookieModel_Table.name eq name)).querySingle()

/**
 * Loads cookies sorted by name
 */
fun loadFbCookiesAsync(callback: (cookies: List<CookieModel>) -> Unit) {
    (select from CookieModel::class).orderBy(CookieModel_Table.name, true).async()
        .queryListResultCallback { _, tResult -> callback(tResult) }.execute()
}

fun loadFbCookiesSync(): List<CookieModel> =
    (select from CookieModel::class).orderBy(CookieModel_Table.name, true).queryList()

inline fun saveFbCookie(cookie: CookieModel, crossinline callback: (() -> Unit) = {}) {
    cookie.async save {
        L.d { "Fb cookie saved" }
        L._d { cookie }
        callback()
    }
}

fun removeCookie(id: Long) {
    loadFbCookie(id)?.async?.delete {
        L.d { "Fb cookie deleted" }
        L._d { id }
    }
}

inline fun CookieModel.fetchUsername(crossinline callback: (String) -> Unit): Disposable =
    ReactiveNetwork.checkInternetConnectivity().subscribeOn(Schedulers.io()).subscribe { yes, _ ->
        if (!yes) return@subscribe callback("")
        var result = ""
        try {
            result = frostJsoup(cookie, FbItem.PROFILE.url).title()
            L.d { "Fetch username found" }
        } catch (e: Exception) {
            if (e !is UnknownHostException)
                e.logFrostEvent("Fetch username failed")
        } finally {
            if (result.isBlank() && (name?.isNotBlank() == true)) {
                callback(name!!)
                return@subscribe
            }
            if (name != result) {
                name = result
                saveFbCookie(this@fetchUsername)
            }
            callback(result)
        }
    }
