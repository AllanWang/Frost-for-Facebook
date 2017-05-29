package com.pitchedapps.frost

import android.app.Application
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-05-28.
 */
class FrostApp : Application() {

    companion object {
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        super.onCreate()
    }
}