package com.pitchedapps.frost.utils

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.pitchedapps.frost.BuildConfig


/**
 * Created by Allan Wang on 2017-05-28.
 *
 * Logging for frost
 *
 * To ensure privacy, the following rules are set:
 *
 * Debug and Error logs must not reveal person info
 * Person info logs can be marked as info or verbose
 */
object L {

    const val TAG = "Frost"

    inline fun v(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            logImpl(Log.VERBOSE, message)
    }

    inline fun i(message: () -> Any?) {
        logImpl(Log.INFO, message)
    }

    inline fun _i(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            logImpl(Log.INFO, message)
    }

    inline fun d(message: () -> Any?) {
        if (BuildConfig.DEBUG || Prefs.verboseLogging)
            logImpl(Log.DEBUG, message)
    }

    inline fun _d(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            logImpl(Log.DEBUG, message)
    }

    inline fun e(t: Throwable? = null, message: () -> Any?) {
        logImpl(Log.ERROR, message, t)
    }

    fun eThrow(message: Any) {
        val msg = message.toString()
        logImpl(Log.ERROR, { msg }, Throwable(msg))
    }

    inline fun logImpl(priority: Int, message: () -> Any?, t: Throwable? = null) {
        val msg = message()?.toString()
        if (BuildConfig.DEBUG) {
            if (t != null)
                Log.e(TAG, msg, t)
            else
                Log.println(priority, TAG, msg ?: "null")
        } else {
            if (msg != null)
                Crashlytics.log(priority, TAG, msg)
            if (t != null)
                Crashlytics.logException(t)
        }
    }
}