package com.pitchedapps.frost.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.WorkerThread
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.injectors.CssHider
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.createFreshFile
import com.pitchedapps.frost.utils.isFacebookUrl
import org.jetbrains.anko.withAlpha
import java.io.File

/**
 * Created by Allan Wang on 2018-01-05.
 *
 * A barebone webview with a refresh listener
 */
class DebugWebView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    var onPageFinished: (String?) -> Unit = {}

    init {
        setupWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebview() {
        settings.javaScriptEnabled = true
        settings.userAgentString = USER_AGENT_BASIC
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webViewClient = DebugClient()
        isDrawingCacheEnabled = true
    }

    @WorkerThread
    fun getScreenshot(output: File): Boolean {

        if (!output.createFreshFile()) {
            L.e { "Failed to create ${output.absolutePath} for debug screenshot" }
            return false
        }
        return try {
            output.outputStream().use {
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
                    else Color.WHITE)
        }


        override fun onPageCommitVisible(view: WebView, url: String?) {
            super.onPageCommitVisible(view, url)
            injectBackgroundColor()
            if (url.isFacebookUrl)
                view.jsInject(
                        CssAssets.ROUND_ICONS.maybe(Prefs.showRoundedIcons),
//                        CssHider.CORE,
                        CssHider.COMPOSER.maybe(!Prefs.showComposer),
                        CssHider.PEOPLE_YOU_MAY_KNOW.maybe(!Prefs.showSuggestedFriends),
                        CssHider.SUGGESTED_GROUPS.maybe(!Prefs.showSuggestedGroups),
                        Prefs.themeInjector,
                        CssHider.NON_RECENT.maybe((url?.contains("?sk=h_chr") ?: false)
                                && Prefs.aggressiveRecents))
        }
    }

}