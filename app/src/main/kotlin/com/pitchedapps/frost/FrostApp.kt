package com.pitchedapps.frost

import android.app.Application
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.CrashReportingTree
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.Showcase
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import timber.log.Timber.DebugTree
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
        Prefs.initialize(this, "${BuildConfig.APPLICATION_ID}.prefs")
        Showcase.initialize(this, "${BuildConfig.APPLICATION_ID}.showcase")
        //        if (LeakCanary.isInAnalyzerProcess(this)) return
//        refWatcher = LeakCanary.install(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
//            LeakCanary.enableDisplayLeakActivity(this)
        } else {
            Fabric.with(this, Crashlytics(), Answers())
            Crashlytics.setUserIdentifier(Prefs.frostId)
            Timber.plant(CrashReportingTree())
        }
        Prefs.verboseLogging = false
        FbCookie()
        if (Prefs.installDate == -1L) Prefs.installDate = System.currentTimeMillis()
        if (Prefs.identifier == -1) Prefs.identifier = Random().nextInt(Int.MAX_VALUE)
        Prefs.lastLaunch = System.currentTimeMillis()



        super.onCreate()

        //Drawer profile loading logic
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String) {
                Glide.with(imageView.context).load(uri).apply(RequestOptions().placeholder(placeholder)).into(imageView)
            }
        })
    }


}