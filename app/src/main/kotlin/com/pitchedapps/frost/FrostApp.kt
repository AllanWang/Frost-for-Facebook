package com.pitchedapps.frost

import android.app.Activity
import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import ca.allanwang.kau.logging.KL
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ApplicationVersionSignature
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.Showcase
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import io.fabric.sdk.android.Fabric
import java.util.*


/**
 * Created by Allan Wang on 2017-05-28.
 */
class FrostApp : Application() {

//    companion object {
//        fun refWatcher(c: Context) = (c.applicationContext as FrostApp).refWatcher
//    }

//    lateinit var refWatcher: RefWatcher

    override fun onCreate() {
        FlowManager.init(FlowConfig.Builder(this).build())
        Showcase.initialize(this, "${BuildConfig.APPLICATION_ID}.showcase")
        Prefs.initialize(this, "${BuildConfig.APPLICATION_ID}.prefs")
        //        if (LeakCanary.isInAnalyzerProcess(this)) return
//        refWatcher = LeakCanary.install(this)
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics(), Answers())
            Crashlytics.setUserIdentifier(Prefs.frostId)
        }
        KL.debug(BuildConfig.DEBUG)
        L.debug(BuildConfig.DEBUG)
        Prefs.verboseLogging = false
        FbCookie()
        if (Prefs.installDate == -1L) Prefs.installDate = System.currentTimeMillis()
        if (Prefs.identifier == -1) Prefs.identifier = Random().nextInt(Int.MAX_VALUE)
        Prefs.lastLaunch = System.currentTimeMillis()

        super.onCreate()

        /**
         * Drawer profile loading logic
         * Reload the image on every version update
         */
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String) {
                val c = imageView.context
                val old = Glide.with(c).load(uri).apply(RequestOptions().placeholder(placeholder))
                Glide.with(c).load(uri).apply(RequestOptions().signature(ApplicationVersionSignature.obtain(c)))
                        .thumbnail(old).into(imageView)
            }
        })
        if (BuildConfig.DEBUG)
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityDestroyed(activity: Activity) {
                    L.d("Activity ${activity.localClassName} destroyed")
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {
                    L.d("Activity ${activity.localClassName} created")
                }
            })
    }


}