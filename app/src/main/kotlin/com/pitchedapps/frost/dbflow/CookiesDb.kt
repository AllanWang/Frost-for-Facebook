package com.pitchedapps.frost.dbflow

import android.os.Parcel
import android.os.Parcelable
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.utils.L
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.structure.BaseModel
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
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
    (select from CookieModel::class).orderBy(CookieModel_Table.name, true).async().queryListResultCallback { _, tResult -> callback(tResult) }.execute()
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

fun CookieModel.fetchUsername(callback: (String) -> Unit) {
    doAsync {
        var result = ""
        try {
            result = Jsoup.connect(FbTab.PROFILE.url)
                    .cookie(FACEBOOK_COM, cookie)
                    .get().title()
            L.d("User name found: $result")
        } catch (e: Exception) {
            L.e("User name fetching failed: ${e.message}")
        } finally {
            if (result.isBlank() && (name?.isNotBlank() ?: false)) {
                callback(name!!)
                return@doAsync
            }
            if (name != result) {
                name = result
                saveFbCookie(this@fetchUsername)
            }
            callback(result)
        }
    }
}