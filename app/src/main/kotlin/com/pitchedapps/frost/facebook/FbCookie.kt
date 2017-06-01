package com.pitchedapps.frost.facebook

import android.webkit.CookieManager
import com.pitchedapps.frost.dbflow.FB_URL_BASE
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.removeCookie
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.events.WebEvent
import com.pitchedapps.frost.utils.GlideUtils
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import org.greenrobot.eventbus.EventBus

/**
 * Created by Allan Wang on 2017-05-30.
 */
object FbCookie {

    var dbCookie: String? = null
    var webCookie: String?
        get() = CookieManager.getInstance().getCookie(FB_URL_BASE)
        set(value) {
            CookieManager.getInstance().setCookie(FB_URL_BASE, value)
            CookieManager.getInstance().flush()
        }

    operator fun invoke() {
        L.d("User ${Prefs.userId}")
        dbCookie = loadFbCookie(Prefs.userId)?.cookie
        if (dbCookie != null && webCookie == null) {
            L.d("DbCookie found & WebCookie is null; setting webcookie")
            webCookie = dbCookie
        }
    }

    private val userMatcher: Regex by lazy { Regex("c_user=([0-9]*);") }

    fun checkUserId(url: String, cookie: String?) {
        if (Prefs.userId != Prefs.userIdDefault || cookie == null) return
        L.d("Checking cookie for $url\n\t$cookie")
        if (!url.contains("facebook") || !cookie.contains(userMatcher)) return
        val id = userMatcher.find(cookie)?.groups?.get(1)?.value
        if (id != null) {
            try {
                save(id.toLong())
            } catch (e: NumberFormatException) {
                //todo send report that id has changed
            }
        }
    }

    fun save(id: Long) {
        L.d("New cookie found for $id")
        Prefs.userId = id
        CookieManager.getInstance().flush()
        EventBus.getDefault().post(WebEvent(WebEvent.REFRESH_BASE))
        saveFbCookie(Prefs.userId, webCookie)
        GlideUtils.downloadProfile(id)
    }

    //TODO reset when new account is added; reset and clear when account is logged out
    fun reset() {
        Prefs.userId = Prefs.userIdDefault
        with(CookieManager.getInstance()) {
            removeAllCookies(null)
            flush()
        }
    }

    fun switchUser(id: Long) {
        val cookie = loadFbCookie(id) ?: return
        Prefs.userId = id
        dbCookie = cookie.cookie
        webCookie = dbCookie
    }

    fun logout() {
        L.d("Logging out user ${Prefs.userId}")
        removeCookie(Prefs.userId)
        reset()
    }
}