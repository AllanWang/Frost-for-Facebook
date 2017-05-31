package com.pitchedapps.frost.facebook

import android.webkit.CookieManager
import com.pitchedapps.frost.dbflow.FB_URL_BASE
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-05-30.
 */
object FbCookie {

    var userId: Long = Prefs.userIdDefault
    var dbCookie: String? = null
    var webCookie: String?
        get() = CookieManager.getInstance().getCookie(FB_URL_BASE)
        set(value) = CookieManager.getInstance().setCookie(FB_URL_BASE, value)

    fun init() {
        userId = Prefs.userId
        dbCookie = loadFbCookie()?.cookie
        if (dbCookie != null && webCookie == null) {
            L.d("DbCookie found & WebCookie is null; setting webcookie")
            webCookie = dbCookie
        }
    }

    private val userMatcher: Regex by lazy { Regex("c_user=([0-9]*);") }

    fun checkUserId(url: String, cookie: String?) {
        if (userId != Prefs.userIdDefault || cookie == null) return
        L.d("Checking cookie for $url\n\t$cookie")
        if (!url.contains("facebook") || !cookie.contains(userMatcher)) return
        val id = userMatcher.find(cookie)?.groups?.get(1)?.value
        if (id != null) {
            try {
                userId = id.toLong()
                save()
            } catch (e: NumberFormatException) {
                //todo send report that id has changed
            }
        }
    }

    fun save() {
        L.d("New cookie found for $userId")
        Prefs.userId = userId
        CookieManager.getInstance().flush()
        saveFbCookie()
    }

    //TODO reset when new account is added; reset and clear when account is logged out
    fun reset() {
        Prefs.userId = Prefs.userIdDefault
        userId = Prefs.userIdDefault
        with(CookieManager.getInstance()) {
            removeAllCookies(null)
            flush()
        }
    }
}