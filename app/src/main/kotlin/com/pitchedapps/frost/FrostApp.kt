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
package com.pitchedapps.frost

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import ca.allanwang.kau.logging.KL
import ca.allanwang.kau.utils.buildIsLollipopAndUp
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.pitchedapps.frost.db.CookieDao
import com.pitchedapps.frost.db.NotificationDao
import com.pitchedapps.frost.facebook.requests.httpClient
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.services.scheduleNotificationsFromPrefs
import com.pitchedapps.frost.services.setupNotificationChannels
import com.pitchedapps.frost.utils.FrostPglAdBlock
import com.pitchedapps.frost.utils.L
import dagger.hilt.android.HiltAndroidApp
import java.util.Random
import javax.inject.Inject

/**
 * Created by Allan Wang on 2017-05-28.
 */
@HiltAndroidApp
class FrostApp : Application() {

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var themeProvider: ThemeProvider

    @Inject
    lateinit var cookieDao: CookieDao

    @Inject
    lateinit var notifDao: NotificationDao

    override fun onCreate() {
        super.onCreate()

        if (!buildIsLollipopAndUp) return // not supported

        initPrefs()

        L.i { "Begin Frost for Facebook" }
        FrostPglAdBlock.init(this)

        setupNotificationChannels(this, themeProvider)

        scheduleNotificationsFromPrefs(prefs)

        BigImageViewer.initialize(GlideImageLoader.with(this, httpClient))

        if (BuildConfig.DEBUG) {
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityDestroyed(activity: Activity) {
                    L.d { "Activity ${activity.localClassName} destroyed" }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    L.d { "Activity ${activity.localClassName} created" }
                }
            })
        }
    }

    private fun initPrefs() {
        prefs.deleteKeys("search_bar", "shown_release", "experimental_by_default")
        KL.shouldLog = { BuildConfig.DEBUG }
        L.shouldLog = {
            when (it) {
                Log.VERBOSE -> BuildConfig.DEBUG
                Log.INFO, Log.ERROR -> true
                else -> BuildConfig.DEBUG || prefs.verboseLogging
            }
        }
        prefs.verboseLogging = false
        if (prefs.installDate == -1L) {
            prefs.installDate = System.currentTimeMillis()
        }
        if (prefs.identifier == -1) {
            prefs.identifier = Random().nextInt(Int.MAX_VALUE)
        }
        prefs.lastLaunch = System.currentTimeMillis()
    }
}
