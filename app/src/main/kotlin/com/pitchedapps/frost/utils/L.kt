package com.pitchedapps.frost.utils

import android.util.Log
import ca.allanwang.kau.logging.KauLogger
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
object L : KauLogger("Frost") {

    override fun logImpl(priority: Int, message: String?, privateMessage: String?, t: Throwable?) {
        if (BuildConfig.DEBUG) {
            super.logImpl(priority, message, privateMessage, t)
        } else {
            if (message != null)
                Crashlytics.log(priority, "Frost", message)
            if (t != null) Crashlytics.logException(t)
        }
    }
}