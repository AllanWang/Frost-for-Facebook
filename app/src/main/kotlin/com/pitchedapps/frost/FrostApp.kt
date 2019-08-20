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
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import ca.allanwang.kau.logging.KL
import ca.allanwang.kau.utils.buildIsLollipopAndUp
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ApplicationVersionSignature
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.pitchedapps.frost.db.FrostDatabase
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.services.scheduleNotificationsFromPrefs
import com.pitchedapps.frost.services.setupNotificationChannels
import com.pitchedapps.frost.utils.BuildUtils
import com.pitchedapps.frost.utils.FrostPglAdBlock
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.Showcase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.Random

/**
 * Created by Allan Wang on 2017-05-28.
 */
class FrostApp : Application() {

//    companion object {
//        fun refWatcher(c: Context) = (c.applicationContext as FrostApp).refWatcher
//    }

//    lateinit var refWatcher: RefWatcher

    override fun onCreate() {
        if (!buildIsLollipopAndUp) { // not supported
            super.onCreate()
            return
        }

//        if (LeakCanary.isInAnalyzerProcess(this)) return
//        refWatcher = LeakCanary.install(this)
        initPrefs()
        initBugsnag()

        L.i { "Begin Frost for Facebook" }
        FrostPglAdBlock.init(this)

        super.onCreate()

        setupNotificationChannels(applicationContext)

        scheduleNotificationsFromPrefs()

        /**
         * Drawer profile loading logic
         * Reload the image on every version update
         */
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String) {
                val c = imageView.context
                val request = GlideApp.with(c)
                val old = request.load(uri).apply(RequestOptions().placeholder(placeholder))
                request.load(uri).apply(
                    RequestOptions()
                        .signature(ApplicationVersionSignature.obtain(c))
                )
                    .thumbnail(old).into(imageView)
            }
        })
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
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@FrostApp)
            modules(FrostDatabase.module(this@FrostApp))
        }
    }

    private fun initPrefs() {
        Showcase.initialize(this, "${BuildConfig.APPLICATION_ID}.showcase")
        Prefs.initialize(this, "${BuildConfig.APPLICATION_ID}.prefs")
        KL.shouldLog = { BuildConfig.DEBUG }
        Prefs.verboseLogging = false
        if (Prefs.installDate == -1L) {
            Prefs.installDate = System.currentTimeMillis()
        }
        if (Prefs.identifier == -1) {
            Prefs.identifier = Random().nextInt(Int.MAX_VALUE)
        }
        Prefs.lastLaunch = System.currentTimeMillis()
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
            notifyReleaseStages = BuildUtils.getAllStages()
            autoCaptureSessions = Prefs.analytics
            enableExceptionHandler = Prefs.analytics
        }
        Bugsnag.init(this, config)
        L.bugsnagInit = true
        Bugsnag.setUserId(Prefs.frostId)
        Bugsnag.addToTab("Build", "Application", BuildConfig.APPLICATION_ID)
        Bugsnag.addToTab("Build", "Version", BuildConfig.VERSION_NAME)

        Bugsnag.beforeNotify { error ->
            when {
                error.exception.stackTrace.any { it.className.contains("XposedBridge") } -> false
                else -> true
            }
        }
    }
}
