package com.pitchedapps.frost

import android.app.Application
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.retro.FrostApi
import com.pitchedapps.frost.utils.CrashReportingTree
import com.pitchedapps.frost.utils.Prefs
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import timber.log.Timber
import timber.log.Timber.DebugTree


/**
 * Created by Allan Wang on 2017-05-28.
 */
class FrostApp : Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) Timber.plant(DebugTree())
        else Timber.plant(CrashReportingTree())
        FlowManager.init(FlowConfig.Builder(this).build())
        Prefs(this)
        FrostApi(this)
        FbCookie.init()
        super.onCreate()
    }


}