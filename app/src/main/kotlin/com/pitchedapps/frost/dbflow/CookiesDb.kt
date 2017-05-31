package com.pitchedapps.frost.dbflow

import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import okhttp3.Cookie
import java.io.Serializable

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Database(name = CookiesDb.NAME, version = CookiesDb.VERSION)
object CookiesDb {
    const val NAME = "Cookies"
    const val VERSION = 1
}

//@Database(name = CookieDb.NAME, version = CookieDb.VERSION)
//object CookieDb {
//    const val NAME = "Cookie"
//    const val VERSION = 1
//}

@Table(database = CookiesDb::class, allFields = true)
data class CookieModel(@PrimaryKey var name: String,
                       var value: String,
                       var expiresAt: Long,
                       var domain: String,
                       var path: String,
                       var secure: Boolean,
                       var httpOnly: Boolean) {

    constructor(cookie: Cookie) : this(cookie.name(), cookie.value(), cookie.expiresAt(), cookie.domain(), cookie.path(), cookie.secure(), cookie.httpOnly())
    constructor() : this("", "", 0L, "", "", false, false)

    fun toCookie(): Cookie {
        val builder = Cookie.Builder().name(name).value(value).expiresAt(expiresAt).domain(domain).path(path)
        if (secure) builder.secure()
        if (httpOnly) builder.httpOnly()
        return builder.build()
    }

    fun isSecure() = secure
    fun isHttpOnly() = httpOnly
}

//class CookieList(val cookies: List<Cookie>)
//class CookieDbList(val cookies: List<CookieDb>)

//@com.raizlabs.android.dbflow.annotation.TypeConverter
//class CookieTypeConverter() : TypeConverter<CookieDbList, CookieList>() {
//    override fun getModelValue(data: CookieDbList): CookieList = CookieList(data.cookies.map { it.toCookie() })
//    override fun getDBValue(model: CookieList): CookieDbList = CookieDbList(model.cookies.map { CookieDb(it) })
//}

@Table(database = CookiesDb::class)
data class Cookies(@PrimaryKey var url: String = "", @ForeignKey var cookie: CookieModel = CookieModel()) : BaseModel(), Serializable