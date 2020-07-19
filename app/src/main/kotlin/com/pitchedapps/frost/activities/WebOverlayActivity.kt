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

import android.content.Intent
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import ca.allanwang.kau.swipe.SwipeBackContract
import ca.allanwang.kau.swipe.kauSwipeOnCreate
import ca.allanwang.kau.swipe.kauSwipeOnDestroy
import ca.allanwang.kau.utils.ContextHelper
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.copyToClipboard
import ca.allanwang.kau.utils.darken
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.finishSlideOut
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.utils.shareText
import ca.allanwang.kau.utils.startLink
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.toDrawable
import ca.allanwang.kau.utils.toast
import ca.allanwang.kau.utils.withAlpha
import ca.allanwang.kau.utils.withMainContext
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.ActivityContract
import com.pitchedapps.frost.contracts.FileChooserContract
import com.pitchedapps.frost.contracts.FileChooserDelegate
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.enums.OverlayContext
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.USER_AGENT
import com.pitchedapps.frost.facebook.USER_AGENT_DESKTOP_CONST
import com.pitchedapps.frost.facebook.USER_AGENT_MOBILE_CONST
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.kotlin.subscribeDuringJob
import com.pitchedapps.frost.utils.ARG_URL
import com.pitchedapps.frost.utils.ARG_USER_ID
import com.pitchedapps.frost.utils.BiometricUtils
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostSnackbar
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.views.FrostContentWeb
import com.pitchedapps.frost.views.FrostVideoViewer
import com.pitchedapps.frost.views.FrostWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Created by Allan Wang on 2017-06-01.
 *
 * Collection of overlay activities for Frost
 *
 * Each one is largely the same layout, but is separated so they may run is separate single tasks
 * All overlays support user switches
 */

/**
 * Used by notifications. Unlike the other overlays, this runs as a singleInstance
 * Going back will bring you back to the previous app
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
class FrostWebActivity : WebOverlayActivityBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val requiresAction = !parseActionSend()
        super.onCreate(savedInstanceState)
        if (requiresAction) {
            /*
             * Signifies that we need to let the user know of a bad url
             * We will subscribe to the load cycle once,
             * and pop a dialog giving the user the option to copy the shared text
             */
            val refreshReceiver = content.refreshChannel.openSubscription()
            content.scope.launch(Dispatchers.IO) {
                refreshReceiver.receive()
                refreshReceiver.cancel()
                withMainContext {
                    materialDialog {
                        title(R.string.invalid_share_url)
                        message(R.string.invalid_share_url_desc)
                    }
                }
            }
        }
    }

    /**
     * Attempts to parse the action url
     * Returns [true] if no action exists or if the action has been consumed, [false] if we need to notify the user of a bad action
     */
    private fun parseActionSend(): Boolean {
        if (intent.action != Intent.ACTION_SEND || intent.type != "text/plain") return true
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return true
        val url = text.toHttpUrlOrNull()?.toString()
        return if (url == null) {
            L.i { "Attempted to share a non-url" }
            L._i { "Shared text: $text" }
            copyToClipboard(text, "Text to Share", showToast = false)
            intent.putExtra(ARG_URL, FbItem.FEED.url)
            false
        } else {
            L.i { "Sharing url through overlay" }
            L._i { "Url: $url" }
            intent.putExtra(ARG_URL, "${FB_URL_BASE}sharer/sharer.php?u=$url")
            true
        }
    }
}

/**
 * Variant that forces a mobile user agent. This is largely internal,
 * and is only necessary when we are launching from an existing [WebOverlayActivityBase]
 */
class WebOverlayMobileActivity : WebOverlayActivityBase(USER_AGENT_MOBILE_CONST)

/**
 * Variant that forces a desktop user agent. This is largely internal,
 * and is only necessary when we are launching from an existing [WebOverlayActivityBase]
 */
class WebOverlayDesktopActivity : WebOverlayActivityBase(USER_AGENT_DESKTOP_CONST)

/**
 * Internal overlay for the app; this is tied with the main task and is singleTop as opposed to singleInstance
 */
class WebOverlayActivity : WebOverlayActivityBase()

@UseExperimental(ExperimentalCoroutinesApi::class)
abstract class WebOverlayActivityBase(private val userAgent: String = USER_AGENT) : BaseActivity(),
    ActivityContract, FrostContentContainer,
    VideoViewHolder, FileChooserContract by FileChooserDelegate() {

    override val frameWrapper: FrameLayout by bindView(R.id.frame_wrapper)
    val toolbar: Toolbar by bindView(R.id.overlay_toolbar)
    val content: FrostContentWeb by bindView(R.id.frost_content_web)
    val web: FrostWebView
        get() = content.coreView
    private val coordinator: CoordinatorLayout by bindView(R.id.overlay_main_content)

    private inline val urlTest: String?
        get() = intent.getStringExtra(ARG_URL) ?: intent.dataString

    lateinit var swipeBack: SwipeBackContract

    /**
     * Nonnull variant; verify by checking [urlTest]
     */
    override val baseUrl: String
        get() = urlTest!!.formattedFbUrl

    override val baseEnum: FbItem? = null

    private inline val userId: Long
        get() = intent.getLongExtra(ARG_USER_ID, prefs.userId)

    private val overlayContext: OverlayContext?
        get() = OverlayContext[intent.extras]

    override fun setTitle(title: String) {
        toolbar.title = title
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (urlTest == null) {
            L.e { "Empty link on web overlay" }
            toast(R.string.null_url_overlay)
            finish()
            return
        }
        setFrameContentView(R.layout.activity_web_overlay)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon = GoogleMaterial.Icon.gmd_close.toDrawable(this, 16, prefs.iconColor)
        toolbar.setNavigationOnClickListener { finishSlideOut() }

        setFrostColors(prefs) {
            toolbar(toolbar)
            themeWindow = false
        }
        coordinator.setBackgroundColor(prefs.bgColor.withAlpha(255))

        content.bind(this)

        content.titleChannel.subscribeDuringJob(this, ContextHelper.coroutineContext) {
            toolbar.title = it
        }

        with(web) {
            userAgentString = userAgent
            prefs.prevId = prefs.userId
            launch {
                val authDefer = BiometricUtils.authenticate(this@WebOverlayActivityBase, prefs)
                if (userId != prefs.userId) {
                    fbCookie.switchUser(userId)
                }
                authDefer.await()
                reloadBase(true)
                if (prefs.firstWebOverlay) {
                    coordinator.frostSnackbar(R.string.web_overlay_swipe_hint) {
                        duration = BaseTransientBottomBar.LENGTH_INDEFINITE
                        setAction(R.string.kau_got_it) { dismiss() }
                    }
                }
            }
        }

        swipeBack = kauSwipeOnCreate {
            if (!prefs.overlayFullScreenSwipe) edgeSize = 20.dpToPx
            transitionSystemBars = false
        }
    }

    /**
     * Manage url loadings
     * This is usually only called when multiple listeners are added and inject the same url
     * We will avoid reloading if the url is the same
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        L.d { "New intent" }
        val newUrl = (intent.getStringExtra(ARG_URL) ?: intent.dataString)?.formattedFbUrl ?: return
        if (baseUrl != newUrl) {
            this.intent = intent
            content.baseUrl = newUrl
            web.reloadBase(true)
        }
    }

    override fun backConsumer(): Boolean {
        if (!web.onBackPressed())
            finishSlideOut()
        return true
    }

    /**
     * Our theme for the overlay should be fully opaque
     */
    fun theme() {
        val opaqueAccent = prefs.headerColor.withAlpha(255)
        statusBarColor = opaqueAccent.darken()
        navigationBarColor = opaqueAccent
        toolbar.setBackgroundColor(opaqueAccent)
        toolbar.setTitleTextColor(prefs.iconColor)
        coordinator.setBackgroundColor(prefs.bgColor.withAlpha(255))
        toolbar.overflowIcon?.setTint(prefs.iconColor)
    }

    override fun onResume() {
        super.onResume()
        web.resumeTimers()
    }

    override fun onPause() {
        web.pauseTimers()
        L.v { "Pause overlay web timers" }
        super.onPause()
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
        kauSwipeOnDestroy()
    }

    override fun openFileChooser(
        filePathCallback: ValueCallback<Array<Uri>?>,
        fileChooserParams: WebChromeClient.FileChooserParams
    ) {
        openMediaPicker(filePathCallback, fileChooserParams)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (onActivityResultWeb(requestCode, resultCode, data)) return
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_web, menu)
        overlayContext?.onMenuCreate(this, menu)
        toolbar.tint(prefs.iconColor)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val url = web.currentUrl.formattedFbUrl
        when (item.itemId) {
            R.id.action_copy_link -> copyToClipboard(url)
            R.id.action_share -> shareText(url)
            R.id.action_open_in_browser -> startLink(url)
            else -> if (!OverlayContext.onOptionsItemSelected(web, item.itemId))
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    /*
     * ----------------------------------------------------
     * Video Contract
     * ----------------------------------------------------
     */
    override var videoViewer: FrostVideoViewer? = null
    override val lowerVideoPadding: PointF = PointF(0f, 0f)
}
