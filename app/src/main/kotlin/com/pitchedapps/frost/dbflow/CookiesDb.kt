package com.pitchedapps.frost.dbflow

import android.os.Parcel
import android.os.Parcelable
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.utils.logFrostAnswers
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.structure.BaseModel
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import paperparcel.PaperParcel
import java.net.UnknownHostException

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
        @JvmField
        val CREATOR = PaperParcelCookieModel.CREATOR
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
        L.d("Fb cookie saved", cookie.toString())
        callback?.invoke()
    }
}

fun removeCookie(id: Long) {
    loadFbCookie(id)?.async?.delete {
        L.d("Fb cookie deleted", id.toString())
    }
}

fun CookieModel.fetchUsername(callback: (String) -> Unit) {
    ReactiveNetwork.checkInternetConnectivity().subscribeOn(Schedulers.io()).subscribe { yes, _ ->
        if (!yes) return@subscribe callback("")
        var result = ""
        try {
            result = frostJsoup(cookie, FbItem.PROFILE.url).title()
            L.d("Fetch username found", result)
        } catch (e: Exception) {
            if (e !is UnknownHostException)
                e.logFrostAnswers("Fetch username failed")
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
}