package com.pitchedapps.frost

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import ca.allanwang.kau.utils.toast
import com.jude.swipbackhelper.SwipeBackHelper
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-06-17.
 */
class WebExternalActivity : AppCompatActivity() {

    val url: String?
        get() = intent?.dataString

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        L.d("Create wea")
        L.d("URL $url")
        toast(url!!)
        SwipeBackHelper.onCreate(this)
        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(true)
                .setSwipeSensitivity(0.5f)
                .setSwipeRelateEnable(true)
                .setSwipeRelateOffset(300)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        L.d("Intent wea")
    }

    override fun onStart() {
        super.onStart()
        L.d("Start")
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        SwipeBackHelper.onPostCreate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SwipeBackHelper.onDestroy(this)
    }
}