package com.pitchedapps.frost.facebook

import android.webkit.CookieManager
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.removeCookie
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.events.FbAccountEvent
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

    fun hasLoggedIn(url: String, cookie: String?):Boolean {
        if (cookie == null || !url.contains("facebook") || !cookie.contains(userMatcher)) return false
        L.d("Checking cookie for $url\n\t$cookie")
        val id = userMatcher.find(cookie)?.groups?.get(1)?.value
        if (id != null) {
            try {
                save(id.toLong(), -1)
                return true
            } catch (e: NumberFormatException) {
                //todo send report that id has changed
            }
        }
        return false
    }

    fun save(id: Long, sender: Int) {
        L.d("New cookie found for $id")
        Prefs.userId = id
        CookieManager.getInstance().flush()
        val cookie = CookieModel(Prefs.userId, "", webCookie)
        EventBus.getDefault().post(FbAccountEvent(cookie, sender, FbAccountEvent.FLAG_NEW))
        saveFbCookie(cookie)
    }

    //TODO reset when new account is added; reset and clear when account is logged out
    fun reset(loggedOut: Boolean = false, sender: Int) {
        Prefs.userId = Prefs.userIdDefault
        with(CookieManager.getInstance()) {
            removeAllCookies(null)
            flush()
        }
        EventBus.getDefault().post(FbAccountEvent(CookieModel(), sender, if (loggedOut) FbAccountEvent.FLAG_LOGOUT else FbAccountEvent.FLAG_RESET))
    }

    fun switchUser(id: Long, sender: Int) = switchUser(loadFbCookie(id), sender)

    fun switchUser(name: String, sender: Int) = switchUser(loadFbCookie(name), sender)

    fun switchUser(cookie: CookieModel?, sender: Int) {
        if (cookie == null) return
        Prefs.userId = cookie.id
        dbCookie = cookie.cookie
        webCookie = dbCookie
        EventBus.getDefault().post(FbAccountEvent(cookie, sender, FbAccountEvent.FLAG_SWITCH))
    }

    fun logout(sender: Int) {
        L.d("Logging out user ${Prefs.userId}")
        removeCookie(Prefs.userId)
        reset(true, sender)
    }
}