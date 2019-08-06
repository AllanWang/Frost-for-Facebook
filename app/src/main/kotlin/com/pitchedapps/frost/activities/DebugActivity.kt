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
package com.pitchedapps.frost.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.visible
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.createFreshDir
import com.pitchedapps.frost.utils.setFrostColors
import kotlinx.android.synthetic.main.activity_debug.*
import kotlinx.android.synthetic.main.view_main_fab.*
import kotlinx.coroutines.CoroutineExceptionHandler
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by Allan Wang on 05/01/18.
 */
class DebugActivity : KauBaseActivity() {

    companion object {
        const val RESULT_URL = "extra_result_url"
        const val RESULT_SCREENSHOT = "extra_result_screenshot"
        const val RESULT_BODY = "extra_result_body"
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
        debug_webview.loadUrl(FbItem.FEED.url)
        debug_webview.onPageFinished = { swipe_refresh.isRefreshing = false }

        swipe_refresh.setOnRefreshListener(debug_webview::reload)

        fab.visible().setIcon(GoogleMaterial.Icon.gmd_bug_report, Prefs.iconColor)
        fab.backgroundTintList = ColorStateList.valueOf(Prefs.accentColor)
        fab.setOnClickListener { _ ->
            fab.hide()

            val errorHandler = CoroutineExceptionHandler { _, throwable ->
                L.e { "DebugActivity error ${throwable.message}" }
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            launchMain(errorHandler) {
                val parent = baseDir(this@DebugActivity)
                parent.createFreshDir()

                val body: String? = suspendCoroutine { cont ->
                    debug_webview.evaluateJavascript(JsActions.RETURN_BODY.function) {
                        cont.resume(it)
                    }
                }

                val hasScreenshot: Boolean =
                    debug_webview.getScreenshot(File(parent, "screenshot.png"))

                val intent = Intent()
                intent.putExtra(RESULT_URL, debug_webview.url)
                intent.putExtra(RESULT_SCREENSHOT, hasScreenshot)
                if (body != null)
                    intent.putExtra(RESULT_BODY, body)
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
        debug_webview.resumeTimers()
    }

    override fun onPause() {
        debug_webview.pauseTimers()
        super.onPause()
    }

    override fun onBackPressed() {
        if (debug_webview.canGoBack())
            debug_webview.goBack()
        else
            super.onBackPressed()
    }
}
