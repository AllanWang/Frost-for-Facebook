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
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.databinding.ActivityDebugBinding
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.createFreshDir
import com.pitchedapps.frost.utils.setFrostColors
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineExceptionHandler
import org.koin.android.ext.android.inject

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

    private val prefs: Prefs by inject()

    lateinit var binding: ActivityDebugBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.init()
    }

    fun ActivityDebugBinding.init() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        setTitle(R.string.debug_frost)

        setFrostColors(prefs) {
            toolbar(toolbar)
        }
        debugWebview.loadUrl(FbItem.FEED.url)
        debugWebview.onPageFinished = { swipeRefresh.isRefreshing = false }

        swipeRefresh.setOnRefreshListener(debugWebview::reload)

        fab.visible().setIcon(GoogleMaterial.Icon.gmd_bug_report, prefs.iconColor)
        fab.backgroundTintList = ColorStateList.valueOf(prefs.accentColor)
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
                    debugWebview.evaluateJavascript(JsActions.RETURN_BODY.function) {
                        cont.resume(it)
                    }
                }

                val hasScreenshot: Boolean =
                    debugWebview.getScreenshot(File(parent, "screenshot.png"))

                val intent = Intent()
                intent.putExtra(RESULT_URL, debugWebview.url)
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
        binding.debugWebview.resumeTimers()
    }

    override fun onPause() {
        binding.debugWebview.pauseTimers()
        super.onPause()
    }

    override fun onBackPressed() {
        if (binding.debugWebview.canGoBack())
            binding.debugWebview.goBack()
        else
            super.onBackPressed()
    }
}
