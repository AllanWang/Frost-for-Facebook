package com.pitchedapps.frost

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import ca.allanwang.kau.utils.*
import com.jude.swipbackhelper.SwipeBackHelper
import com.pitchedapps.frost.utils.ARG_URL
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.formattedFbUrl
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.web.FrostWebView


/**
 * Created by Allan Wang on 2017-06-01.
 */
open class WebOverlayActivity : AppCompatActivity() {

    val toolbar: Toolbar by bindView(R.id.overlay_toolbar)
    val frostWeb: FrostWebView by bindView(R.id.overlay_frost_webview)
    val coordinator: CoordinatorLayout by bindView(R.id.overlay_main_content)

    open val url: String
        get() = intent.extras!!.getString(ARG_URL).formattedFbUrl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_overlay)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        frostWeb.web.setupWebview(url)
        frostWeb.web.loadBaseUrl()
        SwipeBackHelper.onCreate(this)
        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(true)
                .setSwipeSensitivity(0.5f)
                .setSwipeRelateEnable(true)
                .setSwipeRelateOffset(300)
        frostWeb.web.addTitleListener({ toolbar.title = it })
        setFrostColors(toolbar, themeWindow = false)
        coordinator.setBackgroundColor(Prefs.bgColor.withAlpha(255))
    }

    /**
     * Our theme for the overlay should be fully opaque
     */
    fun theme() {
        val darkAccent = Prefs.headerColor.darken().withAlpha(255)
        statusBarColor = darkAccent.darken()
        navigationBarColor = darkAccent
        toolbar.setBackgroundColor(darkAccent)
        toolbar.setTitleTextColor(Prefs.iconColor)
        coordinator.setBackgroundColor(Prefs.bgColor.withAlpha(255))
        toolbar.overflowIcon?.setTint(Prefs.iconColor)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        SwipeBackHelper.onPostCreate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SwipeBackHelper.onDestroy(this)
    }

    override fun onBackPressed() {
        if (!frostWeb.onBackPressed()) {
            finish()
            overridePendingTransition(R.anim.kau_fade_in, R.anim.kau_slide_out_right_top)
        }
    }
}