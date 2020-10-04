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
import ca.allanwang.kau.kpref.KPrefFactory
import ca.allanwang.kau.kpref.KPrefFactoryAndroid
import ca.allanwang.kau.logging.KL
import ca.allanwang.kau.utils.buildIsLollipopAndUp
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.pitchedapps.frost.db.FrostDatabase
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.services.scheduleNotificationsFromPrefs
import com.pitchedapps.frost.services.setupNotificationChannels
import com.pitchedapps.frost.utils.BuildUtils
import com.pitchedapps.frost.utils.FrostPglAdBlock
import com.pitchedapps.frost.utils.L
import java.util.Random
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Created by Allan Wang on 2017-05-28.
 */
class FrostApp : Application(), KoinComponent {

    private lateinit var prefs: Prefs

    override fun onCreate() {
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@FrostApp)
            modules(
                listOf(
                    FrostDatabase.module(),
                    prefFactoryModule(),
                    Prefs.module(),
                    FbCookie.module()
                )
            )
        }
        if (!buildIsLollipopAndUp) { // not supported
            super.onCreate()
            return
        }
        prefs = get()
        initPrefs()
//        initBugsnag()

        L.i { "Begin Frost for Facebook" }
        FrostPglAdBlock.init(this)

        super.onCreate()

        setupNotificationChannels(this, prefs)

        scheduleNotificationsFromPrefs(prefs)

        if (BuildConfig.DEBUG) {
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityDestroyed(activity: Activity) {
                    L.d { "Activity ${activity.localClassName} destroyed" }
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}

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

    private fun initBugsnag() {
        if (BuildConfig.DEBUG) {
            return
        }
        if (!BuildConfig.APPLICATION_ID.startsWith("com.pitchedapps.frost")) {
            return
        }
        val version = BuildUtils.match(BuildConfig.VERSION_NAME)
            ?: return L.d { "Bugsnag disabled for ${BuildConfig.VERSION_NAME}" }
        val config = Configuration("83cf680ed01a6fda10fe497d1c0962bb").apply {
            appVersion = version.versionName
            releaseStage = BuildUtils.getStage(BuildConfig.BUILD_TYPE)
            enabledReleaseStages = BuildUtils.getAllStages()
            autoTrackSessions = prefs.analytics
            autoDetectErrors = prefs.analytics
        }
        Bugsnag.start(this, config)
        L.hasAnalytics = { prefs.analytics }
        Bugsnag.setUser(prefs.frostId, null, null)
        Bugsnag.addMetadata("Build", "Application", BuildConfig.APPLICATION_ID)
        Bugsnag.addMetadata("Build", "Version", BuildConfig.VERSION_NAME)

        Bugsnag.addOnError { event ->
            when {
                event.originalError?.stackTrace?.any { it.className.contains("XposedBridge") } == true -> false
                else -> true
            }
        }
    }

    companion object {
        fun prefFactoryModule(): Module = module {
            single<KPrefFactory> {
                KPrefFactoryAndroid(get())
            }
        }
    }
}
