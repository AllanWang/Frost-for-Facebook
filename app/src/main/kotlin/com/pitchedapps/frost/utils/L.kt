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
