package com.pitchedapps.frost.utils

import android.util.Log
import ca.allanwang.kau.logging.KauLogger
import com.crashlytics.android.Crashlytics
import com.pitchedapps.frost.BuildConfig


/**
 * Created by Allan Wang on 2017-05-28.
 *
 * Logging for frost
 */
object L : KauLogger("Frost") {

    inline fun _i(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            logImpl(Log.INFO, message)
    }

    inline fun _d(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            logImpl(Log.DEBUG, message)
    }

    override fun shouldLog(priority: Int) =
            if (!enabled) false
    else if (priority == Log.VERBOSE) BuildConfig.DEBUG
    else true

    override fun logImpl(priority: Int, message: String, t: Throwable?) {
        if (BuildConfig.DEBUG) {
            if (t != null)
                Log.e(tag, message, t)
            else
                Log.println(priority, tag, message)
        } else {
            if (msg != null)
                Crashlytics.log(priority, TAG, msg)
            if (t != null)
                Crashlytics.logException(t)
        }
    }

    inline fun logImpl(priority: Int, message: () -> Any?, t: Throwable? ) {
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