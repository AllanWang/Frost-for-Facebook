package com.pitchedapps.frost.utils

import android.util.Log
import ca.allanwang.kau.logging.TimberLogger
import com.crashlytics.android.Crashlytics
import timber.log.Timber


/**
 * Created by Allan Wang on 2017-05-28.
 */
object L : TimberLogger("Frost")

internal class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG)
            return
        if (message != null) Crashlytics.log(priority, tag ?: "Frost", message)
        if (t != null) Crashlytics.logException(t)
    }
}