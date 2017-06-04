package com.pitchedapps.frost.utils

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber


/**
 * Created by Allan Wang on 2017-05-28.
 */
object L {
    const val TAG = "Frost: %s"
    fun e(s: String) = Timber.e(TAG, s)
    fun d(s: String) = Timber.d(TAG, s)
    fun i(s: String) = Timber.i(TAG, s)
    fun v(s: String) = Timber.v(TAG, s)
}

internal class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG)
            return
        if (message != null) {
            Log.println(priority, tag ?: "Frost", message)
//            Crashlytics.log(priority, tag ?: "Frost", message)
        }
//        if (t != null) Crashlytics.logException(t)
    }
}