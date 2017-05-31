package com.pitchedapps.frost.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Allan Wang on 2017-05-28.
 */

private val PREFERENCE_NAME = "${com.pitchedapps.frost.BuildConfig.APPLICATION_ID}.prefs"
private val LAST_ACTIVE = "last_active"

object Prefs {

    val prefs: Prefs by lazy { this }

    lateinit private var c: Context
    operator fun invoke(c: Context) {
        this.c = c
        lastActive = 0
    }

    private val sp: SharedPreferences by lazy { c.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE) }

    var lastActive: Long
        get() = sp.getLong(LAST_ACTIVE, -1)
        set(value) = set(LAST_ACTIVE, System.currentTimeMillis())

    private fun set(key: String, value: Boolean) = sp.edit().putBoolean(key, value).apply()
    private fun set(key: String, value: Int) = sp.edit().putInt(key, value).apply()
    private fun set(key: String, value: Long) = sp.edit().putLong(key, value).apply()
    private fun set(key: String, value: String) = sp.edit().putString(key, value).apply()
}