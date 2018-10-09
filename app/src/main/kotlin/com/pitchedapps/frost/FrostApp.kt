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
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ApplicationVersionSignature
import com.google.android.exoplayer2.ExoPlaybackException
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.pitchedapps.frost.dbflow.CookiesDb
import com.pitchedapps.frost.dbflow.FbTabsDb
import com.pitchedapps.frost.dbflow.NotificationDb
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.services.scheduleNotifications
import com.pitchedapps.frost.services.setupNotificationChannels
import com.pitchedapps.frost.utils.*
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.ContentResolverNotifier
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import kotlin.reflect.KClass


/**
 * Created by Allan Wang on 2017-05-28.
 */
class FrostApp : Application() {

//    companion object {
//        fun refWatcher(c: Context) = (c.applicationContext as FrostApp).refWatcher
//    }

//    lateinit var refWatcher: RefWatcher

    private fun FlowConfig.Builder.withDatabase(name: String, klass: KClass<*>) =
            addDatabaseConfig(DatabaseConfig.builder(klass.java)
                    .databaseName(name)
                    .modelNotifier(ContentResolverNotifier("${BuildConfig.APPLICATION_ID}.dbflow.provider"))
                    .build())

    override fun onCreate() {
        if (!buildIsLollipopAndUp) { // not supported
            super.onCreate()
            return
        }

        FlowManager.init(FlowConfig.Builder(this)
                .withDatabase(CookiesDb.NAME, CookiesDb::class)
                .withDatabase(FbTabsDb.NAME, FbTabsDb::class)
                .withDatabase(NotificationDb.NAME, NotificationDb::class)
                .build())
        Showcase.initialize(this, "${BuildConfig.APPLICATION_ID}.showcase")
        Prefs.initialize(this, "${BuildConfig.APPLICATION_ID}.prefs")
        //        if (LeakCanary.isInAnalyzerProcess(this)) return
//        refWatcher = LeakCanary.install(this)
        initBugsnag()
        KL.shouldLog = { BuildConfig.DEBUG }
        Prefs.verboseLogging = false
        L.i { "Begin Frost for Facebook" }
        try {
            FbCookie()
        } catch (e: Exception) {
            // no webview found; error will be handled in start activity
        }
        FrostPglAdBlock.init(this)
        if (Prefs.installDate == -1L) Prefs.installDate = System.currentTimeMillis()
        if (Prefs.identifier == -1) Prefs.identifier = Random().nextInt(Int.MAX_VALUE)
        Prefs.lastLaunch = System.currentTimeMillis()

        super.onCreate()

        setupNotificationChannels(applicationContext)

        applicationContext.scheduleNotifications(Prefs.notificationFreq)

        /**
         * Drawer profile loading logic
         * Reload the image on every version update
         */
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String) {
                val c = imageView.context
                val request = GlideApp.with(c)
                val old = request.load(uri).apply(RequestOptions().placeholder(placeholder))
                request.load(uri).apply(RequestOptions()
                        .signature(ApplicationVersionSignature.obtain(c)))
                        .thumbnail(old).into(imageView)
            }
        })
        if (BuildConfig.DEBUG)
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

        RxJavaPlugins.setErrorHandler {
            when (it) {
                is SocketTimeoutException, is UndeliverableException ->
                    L.e { "RxJava common error ${it.message}" }
                else ->
                    L.e(it) { "RxJava error" }
            }
        }

    }

    private fun initBugsnag() {
        if (BuildConfig.DEBUG) return
        Bugsnag.init(this)
        Bugsnag.disableExceptionHandler()
        if (!BuildConfig.APPLICATION_ID.startsWith("com.pitchedapps.frost")) return
        val version = BuildUtils.match(BuildConfig.VERSION_NAME)
                ?: return L.d { "Bugsnag disabled for ${BuildConfig.VERSION_NAME}" }
        Bugsnag.enableExceptionHandler()
        Bugsnag.setNotifyReleaseStages(*BuildUtils.getAllStages())
        Bugsnag.setAppVersion(version.versionName)
        Bugsnag.setReleaseStage(BuildUtils.getStage(BuildConfig.BUILD_TYPE))
        Bugsnag.setAutoCaptureSessions(true)
        Bugsnag.setUserId(Prefs.frostId)
        Bugsnag.addToTab("Build", "Application", BuildConfig.APPLICATION_ID)
        Bugsnag.addToTab("Build", "Version", BuildConfig.VERSION_NAME)

        Bugsnag.beforeNotify { error ->
            when {
                error.exception is UndeliverableException -> false
                error.exception is ExoPlaybackException -> false
                error.exception is SocketTimeoutException -> false
                error.exception is UnknownHostException -> false
                error.exception.stackTrace.any { it.className.contains("XposedBridge") } -> false
                else -> true
            }
        }
    }

}
