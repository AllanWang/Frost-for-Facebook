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
package com.pitchedapps.frost.facebook

import android.app.Activity
import android.content.Context
import android.webkit.CookieManager
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.removeCookie
import com.pitchedapps.frost.dbflow.saveFbCookie
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.launchLogin
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.SingleSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Allan Wang on 2017-05-30.
 */
object FbCookie {

    const val COOKIE_DOMAIN = FACEBOOK_COM

    /**
     * Retrieves the facebook cookie if it exists
     * Note that this is a synchronized call
     */
    inline val webCookie: String?
        get() = CookieManager.getInstance().getCookie(COOKIE_DOMAIN)

    private fun CookieManager.setWebCookie(cookie: String?, callback: (() -> Unit)?) {
        removeAllCookies { _ ->
            if (cookie == null) {
                callback?.invoke()
                return@removeAllCookies
            }
            L.d { "Setting cookie" }
            val cookies = cookie.split(";").map { Pair(it, SingleSubject.create<Boolean>()) }
            cookies.forEach { (cookie, callback) -> setCookie(COOKIE_DOMAIN, cookie) { callback.onSuccess(it) } }
            Observable.zip<Boolean, Unit>(cookies.map { (_, callback) -> callback.toObservable() }) {}
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        callback?.invoke()
                        L.d { "Cookies set" }
                        L._d { cookie }
                        flush()
                    }
        }
    }

    private suspend fun CookieManager.suspendSetWebCookie(cookie: String?): Boolean {
        cookie ?: return true
        L.test { "Orig ${webCookie}" }
        removeAllCookies()
        L.test { "Save $cookie" }
        // Save all cookies regardless of result, then check if all succeeded
        val result = cookie.split(";").map { setSingleWebCookie(it) }.all { it }
        L.test { "AAAA ${webCookie}" }
        flush()
        L.test { "SSSS ${webCookie}" }
        return result
    }

    private suspend fun CookieManager.removeAllCookies(): Boolean = suspendCoroutine { cont ->
        removeAllCookies {
            L.test { "Removed all cookies $webCookie" }
            cont.resume(it)
        }
    }

    private suspend fun CookieManager.setSingleWebCookie(cookie: String): Boolean = suspendCoroutine { cont ->
        setCookie(COOKIE_DOMAIN, cookie.trim()) {
            L.test { "Save single $cookie\n\n\t$webCookie" }
            cont.resume(it)
        }
    }


    operator fun invoke() {
        L.d { "FbCookie Invoke User" }
        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(true)
        val dbCookie = loadFbCookie(Prefs.userId)?.cookie
        if (dbCookie != null && webCookie == null) {
            L.d { "DbCookie found & WebCookie is null; setting webcookie" }
            GlobalScope.launch(Dispatchers.Main) {
                manager.suspendSetWebCookie(dbCookie)
            }
        }
    }

    fun save(id: Long) {
        L.d { "New cookie found" }
        Prefs.userId = id
        CookieManager.getInstance().flush()
        val cookie = CookieModel(Prefs.userId, "", webCookie)
        saveFbCookie(cookie)
    }

    fun reset(callback: () -> Unit) {
        Prefs.userId = -1L
        with(CookieManager.getInstance()) {
            removeAllCookies {
                flush()
                callback()
            }
        }
    }

    fun switchUser(id: Long, callback: () -> Unit) = switchUser(loadFbCookie(id), callback)

    fun switchUser(name: String, callback: () -> Unit) = switchUser(loadFbCookie(name), callback)

    fun switchUser(cookie: CookieModel?, callback: () -> Unit) {
        if (cookie == null) {
            L.d { "Switching User; null cookie" }
            callback()
            return
        }
        L.d { "Switching User" }
        Prefs.userId = cookie.id
        CookieManager.getInstance().setWebCookie(cookie.cookie, callback)
    }

    /**
     * Helper function to remove the current cookies
     * and launch the proper login page
     */
    fun logout(context: Context) {
        val cookies = arrayListOf<CookieModel>()
        if (context is Activity)
            cookies.addAll(context.cookies().filter { it.id != Prefs.userId })
        logout(Prefs.userId) {
            context.launchLogin(cookies, true)
        }
    }

    /**
     * Clear the cookies of the given id
     */
    fun logout(id: Long, callback: () -> Unit) {
        L.d { "Logging out user" }
        removeCookie(id)
        reset(callback)
    }

    /**
     * Notifications may come from different accounts, and we need to switch the cookies to load them
     * When coming back to the main app, switch back to our original account before continuing
     */
    fun switchBackUser(callback: () -> Unit) {
        if (Prefs.prevId == -1L) return callback()
        val prevId = Prefs.prevId
        Prefs.prevId = -1L
        if (prevId != Prefs.userId) {
            switchUser(prevId) {
                L.d { "Switch back user" }
                L._d { "${Prefs.userId} to $prevId" }
                callback()
            }
        } else callback()
    }
}
