package com.pitchedapps.frost.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import ca.allanwang.kau.internal.KauBaseActivity
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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_debug.*
import kotlinx.android.synthetic.main.view_main_fab.*
import java.io.File

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

            val parent = baseDir(this)
            parent.createFreshDir()
            val rxScreenshot = Single.fromCallable {
                debug_webview.getScreenshot(File(parent, "screenshot.png"))
            }.subscribeOn(Schedulers.io())
            val rxBody = Single.create<String> { emitter ->
                debug_webview.evaluateJavascript(JsActions.RETURN_BODY.function) {
                    emitter.onSuccess(it)
                }
            }.subscribeOn(AndroidSchedulers.mainThread())
            Single.zip(listOf(rxScreenshot, rxBody)) {
                val screenshot = it[0] == true
                val body = it[1] as? String
                screenshot to body
            }.observeOn(AndroidSchedulers.mainThread())
                .subscribe { (screenshot, body), err ->
                    if (err != null) {
                        L.e { "DebugActivity error ${err.message}" }
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                        return@subscribe
                    }
                    val intent = Intent()
                    intent.putExtra(RESULT_URL, debug_webview.url)
                    intent.putExtra(RESULT_SCREENSHOT, screenshot)
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