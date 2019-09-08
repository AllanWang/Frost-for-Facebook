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
package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.facebook.USER_AGENT
import com.pitchedapps.frost.injectors.CssHider
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.createFreshFile
import com.pitchedapps.frost.utils.isFacebookUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Created by Allan Wang on 2018-01-05.
 *
 * A barebone webview with a refresh listener
 */
class DebugWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var onPageFinished: (String?) -> Unit = {}

    init {
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = DebugClient()
        @Suppress("DEPRECATION")
        isDrawingCacheEnabled = true
    }

    /**
     * Fetches a screenshot of the current webview, returning true if successful, false otherwise.
     */
    suspend fun getScreenshot(output: File): Boolean = withContext(Dispatchers.IO) {

        if (!output.createFreshFile()) {
            L.e { "Failed to create ${output.absolutePath} for debug screenshot" }
            return@withContext false
        }
        try {
            output.outputStream().use {
                @Suppress("DEPRECATION")
                drawingCache.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            L.d { "Created screenshot at ${output.absolutePath}" }
            true
        } catch (e: Exception) {
            L.e { "An error occurred ${e.message}" }
            false
        }
    }

    private inner class DebugClient : BaseWebViewClient() {

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            onPageFinished(url)
        }

        private fun injectBackgroundColor() {
            setBackgroundColor(
                if (url.isFacebookUrl) Prefs.bgColor.withAlpha(255)
                else Color.WHITE
            )
        }

        override fun onPageCommitVisible(view: WebView, url: String?) {
            super.onPageCommitVisible(view, url)
            injectBackgroundColor()
            if (url.isFacebookUrl)
                view.jsInject(
//                        CssHider.CORE,
                    CssHider.COMPOSER.maybe(!Prefs.showComposer),
                    CssHider.STORIES.maybe(!Prefs.showStories),
                    CssHider.PEOPLE_YOU_MAY_KNOW.maybe(!Prefs.showSuggestedFriends),
                    CssHider.SUGGESTED_GROUPS.maybe(!Prefs.showSuggestedGroups),
                    Prefs.themeInjector,
                    CssHider.NON_RECENT.maybe(
                        (url?.contains("?sk=h_chr") ?: false) &&
                            Prefs.aggressiveRecents
                    )
                )
        }
    }
}
