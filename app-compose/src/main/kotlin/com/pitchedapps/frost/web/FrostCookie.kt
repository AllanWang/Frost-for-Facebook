/*
 * Copyright 2023 Allan Wang
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
package com.pitchedapps.frost.web

import android.content.Context
import android.webkit.CookieManager
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.HTTPS_FACEBOOK_COM
import com.pitchedapps.frost.facebook.HTTPS_MESSENGER_COM
import com.pitchedapps.frost.facebook.MESSENGER_COM
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class FrostCookie @Inject internal constructor(private val cookieManager: CookieManager) {

  val fbCookie: String?
    get() = cookieManager.getCookie(HTTPS_FACEBOOK_COM)

  val messengerCookie: String?
    get() = cookieManager.getCookie(HTTPS_MESSENGER_COM)

  private suspend fun CookieManager.suspendSetWebCookie(domain: String, cookie: String?): Boolean {
    cookie ?: return true
    return withContext(NonCancellable) {
      // Save all cookies regardless of result, then check if all succeeded
      val result =
        cookie.split(";").map { async { setSingleWebCookie(domain, it) } }.awaitAll().all { it }
      logger.atInfo().log("Cookies set for %s, %b", domain, result)
      result
    }
  }

  private suspend fun CookieManager.setSingleWebCookie(domain: String, cookie: String): Boolean =
    suspendCoroutine { cont ->
      setCookie(domain, cookie.trim()) { cont.resume(it) }
    }

  private suspend fun CookieManager.removeAllCookies(): Boolean = suspendCoroutine { cont ->
    removeAllCookies { cont.resume(it) }
  }

  suspend fun save(id: Long) {
    logger.atInfo().log("Saving cookies for %d", id)
    //    prefs.userId = id
    cookieManager.flush()
    //    val cookie = CookieEntity(prefs.userId, null, webCookie)
    //    cookieDao.save(cookie)
  }

  suspend fun reset() {
    //    prefs.userId = -1L
    withContext(Dispatchers.Main + NonCancellable) {
      with(cookieManager) {
        removeAllCookies()
        flush()
      }
    }
  }

  suspend fun switchUser(id: Long) {
    //    val cookie = cookieDao.selectById(id) ?: return L.e { "No cookie for id" }
    //    switchUser(cookie)
  }

  //  suspend fun switchUser(cookie: CookieEntity?) {
  //    if (cookie?.cookie == null) {
  //      L.d { "Switching User; null cookie" }
  //      return
  //    }
  //    withContext(Dispatchers.Main + NonCancellable) {
  //      L.d { "Switching User" }
  //      prefs.userId = cookie.id
  //      CookieManager.getInstance().apply {
  //        removeAllCookies()
  //        suspendSetWebCookie(FB_COOKIE_DOMAIN, cookie.cookie)
  //        suspendSetWebCookie(MESSENGER_COOKIE_DOMAIN, cookie.cookieMessenger)
  //        flush()
  //      }
  //    }
  //  }

  /** Helper function to remove the current cookies and launch the proper login page */
  suspend fun logout(context: Context, deleteCookie: Boolean = true) {
    //    val cookies = arrayListOf<CookieEntity>()
    //    if (context is Activity) cookies.addAll(context.cookies().filter { it.id != prefs.userId
    // })
    //    logout(prefs.userId, deleteCookie)
    //    context.launchLogin(cookies, true)
  }

  /** Clear the cookies of the given id */
  suspend fun logout(id: Long, deleteCookie: Boolean = true) {
    logger.atInfo().log("Logging out user %d", id)
    // TODO save cookies?
    if (deleteCookie) {
      //      cookieDao.deleteById(id)
      logger.atInfo().log("Deleted cookies")
    }
    reset()
  }

  /**
   * Notifications may come from different accounts, and we need to switch the cookies to load them
   * When coming back to the main app, switch back to our original account before continuing
   */
  suspend fun switchBackUser() {
    //    if (prefs.prevId == -1L) return
    //    val prevId = prefs.prevId
    //    prefs.prevId = -1L
    //    if (prevId != prefs.userId) {
    //      switchUser(prevId)
    //      L.d { "Switch back user" }
    //      L._d { "${prefs.userId} to $prevId" }
    //    }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()

    /** Domain information. Dot prefix still matters for Android browsers. */
    private const val FB_COOKIE_DOMAIN = ".$FACEBOOK_COM"
    private const val MESSENGER_COOKIE_DOMAIN = ".$MESSENGER_COM"
  }
}
