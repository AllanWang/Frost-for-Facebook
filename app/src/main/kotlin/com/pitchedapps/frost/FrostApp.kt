package com.pitchedapps.frost

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.services.requestNotifications
import com.pitchedapps.frost.utils.CrashReportingTree
import com.pitchedapps.frost.utils.Prefs
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import timber.log.Timber.DebugTree


/**
 * Created by Allan Wang on 2017-05-28.
 */
class FrostApp : Application() {

//    companion object {
//        fun refWatcher(c: Context) = (c.applicationContext as FrostApp).refWatcher
//    }

//    lateinit var refWatcher: RefWatcher

    override fun onCreate() {
//        if (LeakCanary.isInAnalyzerProcess(this)) return
//        refWatcher = LeakCanary.install(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
//            LeakCanary.enableDisplayLeakActivity(this)
        } else {
            Fabric.with(this, Crashlytics(), Answers())
            Timber.plant(CrashReportingTree())
        }
        FlowManager.init(FlowConfig.Builder(this).build())
        Prefs.initialize(this, "${com.pitchedapps.frost.BuildConfig.APPLICATION_ID}.prefs")
        FbCookie()
        super.onCreate()
        //Drawer profile loading logic
        DrawerImageLoader.init(object : AbstractDrawerImageLoader() {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable, tag: String) {
                Glide.with(imageView.context).load(uri).apply(RequestOptions().placeholder(placeholder)).into(imageView)
            }

            override fun placeholder(ctx: Context, tag: String): Drawable {
                when (tag) {
                    DrawerImageLoader.Tags.PROFILE.name, DrawerImageLoader.Tags.ACCOUNT_HEADER.name -> DrawerUIUtils.getPlaceHolder(ctx)
                }
                return super.placeholder(ctx, tag);
            }
        })
    }


}