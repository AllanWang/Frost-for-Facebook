package com.pitchedapps.frost

import android.app.Application
import com.pitchedapps.frost.facebook.retro.FrostApi
import com.pitchedapps.frost.facebook.retro.IFrost
import com.pitchedapps.frost.utils.CrashReportingTree
import com.pitchedapps.frost.utils.Prefs
import io.realm.Realm
import timber.log.Timber
import timber.log.Timber.DebugTree


/**
 * Created by Allan Wang on 2017-05-28.
 */
class FrostApp : Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) Timber.plant(DebugTree())
        else Timber.plant(CrashReportingTree())
        Prefs(applicationContext)
        FrostApi(applicationContext)
        Realm.init(applicationContext)
        super.onCreate()
    }
}