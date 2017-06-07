package com.pitchedapps.frost.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

/**
 * Created by Allan Wang on 2017-05-28.
 *
 * Shared Preference object with lazy cached retrievals
 */

private val PREFERENCE_NAME = "${com.pitchedapps.frost.BuildConfig.APPLICATION_ID}.prefs"
private val LAST_ACTIVE = "last_active"
private val USER_ID = "user_id"
private val COLOR_TEXT = "color_text"
private val COLOR_BG = "color_bg"
private val COLOR_HEADER = "color_header"
private val COLOR_ICONS = "color_icons"
private val THEME_TYPE = "theme_type"

object Prefs {

    private const val prefDefaultLong = -2L
    private const val prefDefaultInt = -2

    lateinit private var c: Context
    operator fun invoke(c: Context) {
        this.c = c
        lastActive = 0
    }

    private val sp: SharedPreferences by lazy { c.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE) }

    var lastActive: Long = prefDefaultLong
        get() {
            if (field == prefDefaultLong) field = sp.getLong(LAST_ACTIVE, -1)
            return field
        }
        set(value) {
            field = value
            if (value != prefDefaultLong) set(LAST_ACTIVE, System.currentTimeMillis())
        }

    const val userIdDefault = -1L
    var userId: Long = prefDefaultLong
        get() {
            if (field == prefDefaultLong) field = sp.getLong(USER_ID, userIdDefault)
            return field
        }
        set(value) {
            field = value
            if (value != prefDefaultLong) set(USER_ID, value)
        }

    var textColor: Int = prefDefaultInt
        get() {
            if (field == prefDefaultInt) field = sp.getInt(COLOR_TEXT, Color.BLACK)
            return field
        }
        set(value) {
            field = value
            if (value != prefDefaultInt) set(COLOR_TEXT, value)
        }

    var bgColor: Int = prefDefaultInt
        get() {
            if (field == prefDefaultInt) field = sp.getInt(COLOR_BG, Color.WHITE)
            return field
        }
        set(value) {
            field = value
            if (value != prefDefaultInt) set(COLOR_BG, value)
        }

    var headerColor: Int = prefDefaultInt
        get() {
            if (field == prefDefaultInt) field = sp.getInt(COLOR_HEADER, 0xff3b5998.toInt())
            return field
        }
        set(value) {
            field = value
            if (value != prefDefaultInt) set(COLOR_HEADER, value)
        }

    var iconColor: Int = prefDefaultInt
        get() {
            if (field == prefDefaultInt) field = sp.getInt(COLOR_ICONS, Color.WHITE)
            return field
        }
        set(value) {
            field = value
            if (value != prefDefaultInt) set(COLOR_ICONS, value)
        }

    private fun set(key: String, value: Boolean) = sp.edit().putBoolean(key, value).apply()
    private fun set(key: String, value: Int) = sp.edit().putInt(key, value).apply()
    private fun set(key: String, value: Long) = sp.edit().putLong(key, value).apply()
    private fun set(key: String, value: String) = sp.edit().putString(key, value).apply()

    fun clear() {
        L.d("Clearing Prefs")
        sp.edit().clear().apply()
        lastActive = prefDefaultLong
        userId = prefDefaultLong
        textColor = prefDefaultInt
        bgColor = prefDefaultInt
        headerColor = prefDefaultInt
        iconColor = prefDefaultInt
    }
}
