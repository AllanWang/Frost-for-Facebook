package com.pitchedapps.frost.facebook

import android.webkit.CookieManager
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-05-30.
 */
object CookieMap {

    var userId: Int = -1
    private val userMatcher = "c_user=([0-9]*);"
    private val map = HashMap<String, String>()

    operator fun get(key: String) = map[key]

    operator fun set(key: String, value: String) {
        map[key] = value
    }

    fun put(url: String, cookie: String) {
        map.put(url, cookie)
        checkUserId(url, cookie)
    }

    fun checkUserId(url: String, cookie: String) {
        if (userId != -1) return
        if (!url.contains("facebook") || !cookie.contains(userMatcher)) return
        val id = Regex(userMatcher).find(cookie)?.value
        if (id != null) {
            userId = id.toInt()
            save()
        }
    }

    fun save() {
        Prefs.userId = userId
        CookieManager.getInstance().flush()

    }

    fun reset() {

    }
}