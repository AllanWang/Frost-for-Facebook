package com.pitchedapps.frost.utils

import android.util.Log
import ca.allanwang.kau.logging.KauLogger
import com.bugsnag.android.Bugsnag
import com.pitchedapps.frost.BuildConfig

/**
 * Created by Allan Wang on 2017-05-28.
 *
 * Logging for frost
 */
object L : KauLogger("Frost", {
    when (it) {
        Log.VERBOSE -> BuildConfig.DEBUG
        Log.INFO, Log.ERROR -> true
        else -> BuildConfig.DEBUG || Prefs.verboseLogging
    }
}) {

    inline fun test(message: () -> Any?) {
        _d {
            "Test1234 ${message()}"
        }
    }

    inline fun _i(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            i(message)
    }

    inline fun _d(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            d(message)
    }

    override fun logImpl(priority: Int, message: String?, t: Throwable?) {
        if (BuildConfig.DEBUG)
            super.logImpl(priority, message, t)
        else {
            if (message != null)
                Bugsnag.leaveBreadcrumb(message)
            if (t != null)
                Bugsnag.notify(t)
        }
    }
}