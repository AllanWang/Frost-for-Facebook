package com.pitchedapps.frost.facebook

import android.webkit.CookieManager
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.removeCookie
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-05-30.
 */
object FbCookie {

    var webCookie: String?
        get() = CookieManager.getInstance().getCookie(FB_URL_BASE)
        set(value) {
            CookieManager.getInstance().setCookie(FB_URL_BASE, value)
            CookieManager.getInstance().flush()
        }

    operator fun invoke() {
        L.d("User ${Prefs.userId}")
        val dbCookie = loadFbCookie(Prefs.userId)?.cookie
        if (dbCookie != null && webCookie == null) {
            L.d("DbCookie found & WebCookie is null; setting webcookie")
            webCookie = dbCookie
        }
    }

    fun save(id: Long) {
        L.d("New cookie found for $id")
        Prefs.userId = id
        CookieManager.getInstance().flush()
        val cookie = CookieModel(Prefs.userId, "", webCookie)
        saveFbCookie(cookie)
    }

    fun reset() {
        Prefs.userId = Prefs.userIdDefault
        with(CookieManager.getInstance()) {
            removeAllCookies(null)
            flush()
        }
    }

    fun switchUser(id: Long) = switchUser(loadFbCookie(id))

    fun switchUser(name: String) = switchUser(loadFbCookie(name))

    fun switchUser(cookie: CookieModel?) {
        if (cookie == null) return
        Prefs.userId = cookie.id
        webCookie = cookie.cookie
        //TODO add webview refresh event
    }

    fun logout(id:Long) {
        L.d("Logging out user $id")
        removeCookie(id)
        reset()
    }
}