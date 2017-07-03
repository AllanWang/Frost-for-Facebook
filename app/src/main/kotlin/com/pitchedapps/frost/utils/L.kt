package com.pitchedapps.frost.utils

import android.util.Log
import ca.allanwang.kau.logging.TimberLogger
import com.crashlytics.android.Crashlytics
import timber.log.Timber


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
object L : TimberLogger("Frost") {

    /**
     * Helper function to separate private info
     */
    fun d(tag: String, personal: String?) {
        L.d(tag)
        L.i("-\t$personal")
    }
}

internal class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        when (priority) {
            Log.VERBOSE, Log.INFO -> return
            Log.DEBUG -> if (!Prefs.verboseLogging) return
        }
        if (message != null)
            Crashlytics.log(priority, "Frost", message)
        if (t != null) Crashlytics.logException(t)
    }
}