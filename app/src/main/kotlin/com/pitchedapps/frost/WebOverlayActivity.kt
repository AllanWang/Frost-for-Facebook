package com.pitchedapps.frost

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import ca.allanwang.kau.utils.*
import com.jude.swipbackhelper.SwipeBackHelper
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.web.FrostWebView


/**
 * Created by Allan Wang on 2017-06-01.
 */
open class WebOverlayActivity : AppCompatActivity() {

    val toolbar: Toolbar by bindView(R.id.overlay_toolbar)
    val frostWeb: FrostWebView by bindView(R.id.overlay_frost_webview)
    val coordinator: CoordinatorLayout by bindView(R.id.overlay_main_content)

    companion object {
        const val ARG_USER_ID = "arg_user_id"
    }

    open val url: String
        get() = intent.extras!!.getString(ARG_URL).formattedFbUrl

    val userId: Long
        get() = intent.extras?.getLong(ARG_USER_ID, Prefs.userId) ?: Prefs.userId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_overlay)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon = GoogleMaterial.Icon.gmd_close.toDrawable(this, 16, Prefs.iconColor)
        toolbar.setNavigationOnClickListener { finishSlideOut() }
        SwipeBackHelper.onCreate(this)
        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(true)
                .setSwipeSensitivity(0.5f)
                .setSwipeRelateEnable(true)
                .setSwipeRelateOffset(300)
        setFrostColors(toolbar, themeWindow = false)
        coordinator.setBackgroundColor(Prefs.bgColor.withAlpha(255))

        frostWeb.web.setupWebview(url)
        frostWeb.web.addTitleListener({ toolbar.title = it })
        if (userId != Prefs.userId) FbCookie.switchUser(userId) { frostWeb.web.loadBaseUrl() }
        else frostWeb.web.loadBaseUrl()
    }

    /**
     * Manage url loadings
     * This is usually only called when multiple listeners are added and inject the same url
     * We will avoid reloading if the url is the same
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val newUrl = intent.extras!!.getString(ARG_URL).formattedFbUrl
        if (url != newUrl) {
            this.intent = intent
            frostWeb.web.baseUrl = newUrl
            frostWeb.web.loadBaseUrl()
        }
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
            finishSlideOut()
        }
    }
}