package com.pitchedapps.frost.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.visible
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.createFreshDir
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.web.DebugWebView
import java.io.File

/**
 * Created by Allan Wang on 05/01/18.
 */
class DebugActivity : KauBaseActivity() {

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val web: DebugWebView by bindView(R.id.debug_webview)
    private val swipeRefresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    private val fab: FloatingActionButton by bindView(R.id.fab)

    companion object {
        const val RESULT_URL = "extra_result_url"
        fun baseDir(context: Context) = File(context.externalCacheDir, "offline_debug")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        setTitle(R.string.debug_frost)
        
        setFrostColors {
            toolbar(toolbar)
        }
        web.loadUrl(FbItem.FEED.url)
        web.onPageFinished = { swipeRefresh.isRefreshing = false }

        swipeRefresh.setOnRefreshListener(web::reload)

        fab.visible().setIcon(GoogleMaterial.Icon.gmd_bug_report, Prefs.iconColor)
        fab.backgroundTintList = ColorStateList.valueOf(Prefs.accentColor)
        fab.setOnClickListener {
            fab.hide()

            val parent = baseDir(this)
            parent.createFreshDir()
            val file = File(parent, "screenshot.png")
            web.getScreenshot(file) {
                val intent = Intent()
                intent.putExtra(RESULT_URL, web.url)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        web.resumeTimers()
    }

    override fun onPause() {
        web.pauseTimers()
        super.onPause()
    }

    override fun onBackPressed() {
        if (web.canGoBack())
            web.goBack()
        else
            super.onBackPressed()
    }
}