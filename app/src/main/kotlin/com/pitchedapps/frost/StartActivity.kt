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
package com.pitchedapps.frost

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.buildIsLollipopAndUp
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.startActivity
import ca.allanwang.kau.utils.string
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.activities.LoginActivity
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.SelectorActivity
import com.pitchedapps.frost.activities.TestActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.loadFbCookiesSync
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.EXTRA_COOKIES
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.utils.loadAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

/**
 * Created by Allan Wang on 2017-05-28.
 */
class StartActivity : KauBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        startActivity<TestActivity>()
//        return

        if (!buildIsLollipopAndUp) { // not supported
            showInvalidSdkView()
            return
        }

        try {
            // TODO add better descriptions
            CookieManager.getInstance()
        } catch (e: Exception) {
            showInvalidWebView()
        }

        launch {
            try {
                FbCookie.switchBackUser()
                val cookies = ArrayList(withContext(Dispatchers.IO) {
                    loadFbCookiesSync()
                })
                L.i { "Cookies loaded at time ${System.currentTimeMillis()}" }
                L._d { "Cookies: ${cookies.joinToString("\t", transform = CookieModel::toSensitiveString)}" }
                loadAssets()
                when {
                    cookies.isEmpty() -> launchNewTask<LoginActivity>()
                    // Has cookies but no selected account
                    Prefs.userId == -1L -> launchNewTask<SelectorActivity>(cookies)
                    else -> startActivity<MainActivity>(intentBuilder = {
                        putParcelableArrayListExtra(EXTRA_COOKIES, cookies)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                }
            } catch (e: Exception) {
                showInvalidWebView()
            }
        }
    }

    private fun showInvalidWebView() =
        showInvalidView(R.string.error_webview)

    private fun showInvalidSdkView() {
        val text = String.format(string(R.string.error_sdk), Build.VERSION.SDK_INT)
        showInvalidView(text)
    }

    private fun showInvalidView(textRes: Int) =
        showInvalidView(string(textRes))

    private fun showInvalidView(text: String) {
        setContentView(R.layout.activity_invalid)
        findViewById<ImageView>(R.id.invalid_icon)
            .setIcon(GoogleMaterial.Icon.gmd_adb, -1, Color.WHITE)
        findViewById<TextView>(R.id.invalid_text).text = text
    }
}
