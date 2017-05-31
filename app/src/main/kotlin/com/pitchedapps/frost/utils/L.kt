package com.pitchedapps.frost.utils

import android.util.Log
import timber.log.Timber


/**
 * Created by Allan Wang on 2017-05-28.
 */
object L {
    val TAG = "Frost: %s"
    fun e(s: String) = Timber.e(TAG, s)
    fun d(s: String) = Timber.d(TAG, s)
}

internal class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG)
            return
        Log.println(priority, tag, message)
//        FakeCrashLibrary.log(priority, tag, message)

//        if (t != null) {
//            if (priority == Log.ERROR) {
//                FakeCrashLibrary.logError(t)
//            } else if (priority == Log.WARN) {
//                FakeCrashLibrary.logWarning(t)
//            }
//        }
    }
}