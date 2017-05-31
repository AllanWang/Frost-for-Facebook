package com.pitchedapps.frost.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Allan Wang on 2017-05-28.
 */

private val PREFERENCE_NAME = "${com.pitchedapps.frost.BuildConfig.APPLICATION_ID}.prefs"
private val LAST_ACTIVE = "last_active"
private val USER_ID = "user_id"

object Prefs {

    lateinit private var c: Context
    operator fun invoke(c: Context) {
        this.c = c
        lastActive = 0
    }

    private val sp: SharedPreferences by lazy { c.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE) }

    var lastActive: Long
        get() = sp.getLong(LAST_ACTIVE, -1)
        set(value) = set(LAST_ACTIVE, System.currentTimeMillis())

    var userId: Int
        get() = sp.getInt(USER_ID, -1)
        set(value) = set(USER_ID, value)

    private fun set(key: String, value: Boolean) = sp.edit().putBoolean(key, value).apply()
    private fun set(key: String, value: Int) = sp.edit().putInt(key, value).apply()
    private fun set(key: String, value: Long) = sp.edit().putLong(key, value).apply()
    private fun set(key: String, value: String) = sp.edit().putString(key, value).apply()
}