package com.pitchedapps.frost.utils

import com.pitchedapps.frost.FrostApp

/**
 * Created by Allan Wang on 2017-05-28.
 */
val prefs: Prefs by lazy { FrostApp.prefs }

class Prefs(c: android.content.Context) {
    private companion object {
        val PREFERENCE_NAME = "${com.pitchedapps.frost.BuildConfig.APPLICATION_ID}.prefs"
        val LAST_ACTIVE = "last_active"
    }

    var lastActive: Long
        get() = prefs.getLong(com.pitchedapps.frost.utils.Prefs.Companion.LAST_ACTIVE, -1)
        set(value) = set(com.pitchedapps.frost.utils.Prefs.Companion.LAST_ACTIVE, System.currentTimeMillis())

    init {
        lastActive = 0
    }

    private val prefs: android.content.SharedPreferences by lazy { c.getSharedPreferences(com.pitchedapps.frost.utils.Prefs.Companion.PREFERENCE_NAME, android.content.Context.MODE_PRIVATE) }

    private fun set(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    private fun set(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    private fun set(key: String, value: Long) = prefs.edit().putLong(key, value).apply()
    private fun set(key: String, value: String) = prefs.edit().putString(key, value).apply()
}