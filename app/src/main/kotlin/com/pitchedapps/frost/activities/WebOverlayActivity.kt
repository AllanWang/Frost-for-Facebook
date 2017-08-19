package com.pitchedapps.frost.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.swipe.kauSwipeOnCreate
import ca.allanwang.kau.swipe.kauSwipeOnDestroy
import ca.allanwang.kau.swipe.kauSwipeOnPostCreate
import ca.allanwang.kau.utils.*
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.ActivityWebContract
import com.pitchedapps.frost.contracts.FileChooserContract
import com.pitchedapps.frost.contracts.FileChooserDelegate
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.web.FrostWebView


/**
 * Created by Allan Wang on 2017-06-01.
 */
open class WebOverlayActivity : KauBaseActivity(),
        ActivityWebContract, FileChooserContract by FileChooserDelegate() {

    val toolbar: Toolbar by bindView(R.id.overlay_toolbar)
    val frostWeb: FrostWebView by bindView(R.id.overlay_frost_webview)
    val coordinator: CoordinatorLayout by bindView(R.id.overlay_main_content)

    val urlTest: String?
        get() = intent.extras?.getString(ARG_URL) ?: intent.dataString

    open val url: String
        get() = (intent.extras?.getString(ARG_URL) ?: intent.dataString).formattedFbUrl

    val userId: Long
        get() = intent.extras?.getLong(ARG_USER_ID, Prefs.userId) ?: Prefs.userId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (urlTest == null) {
            L.eThrow("Empty link on web overlay")
            toast(R.string.null_url_overlay)
            finish()
            return
        }
        setContentView(R.layout.activity_web_overlay)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon = GoogleMaterial.Icon.gmd_close.toDrawable(this, 16, Prefs.iconColor)
        toolbar.setNavigationOnClickListener { finishSlideOut() }

        setFrostColors(toolbar, themeWindow = false)
        coordinator.setBackgroundColor(Prefs.bgColor.withAlpha(255))

        frostWeb.setupWebview(url)
        frostWeb.web.addTitleListener({ toolbar.title = it })
        if (userId != Prefs.userId) FbCookie.switchUser(userId) { frostWeb.web.loadBaseUrl() }
        else frostWeb.web.loadBaseUrl()
        if (Showcase.firstWebOverlay) {
            coordinator.frostSnackbar(R.string.web_overlay_swipe_hint) {
                duration = Snackbar.LENGTH_INDEFINITE
                setAction(R.string.kau_got_it) { _ -> this.dismiss() }
            }
        }

        kauSwipeOnCreate {
            if (!Prefs.overlayFullScreenSwipe) edgeSize = 20.dpToPx
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
        val newUrl = (intent.extras?.getString(ARG_URL) ?: intent.dataString ?: return).formattedFbUrl
        L.d("New intent")
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
        val opaqueAccent = Prefs.headerColor.withAlpha(255)
        statusBarColor = opaqueAccent.darken()
        navigationBarColor = opaqueAccent
        toolbar.setBackgroundColor(opaqueAccent)
        toolbar.setTitleTextColor(Prefs.iconColor)
        coordinator.setBackgroundColor(Prefs.bgColor.withAlpha(255))
        toolbar.overflowIcon?.setTint(Prefs.iconColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        kauSwipeOnDestroy()
    }

    override fun onBackPressed() {
        if (!frostWeb.onBackPressed()) {
            finishSlideOut()
        }
    }

    override fun openFileChooser(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams) {
        openMediaPicker(filePathCallback, fileChooserParams)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (onActivityResultWeb(requestCode, resultCode, data)) return
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_web, menu)
        toolbar.tint(Prefs.iconColor)
        setMenuIcons(menu, Prefs.iconColor,
                R.id.action_share to CommunityMaterial.Icon.cmd_share,
                R.id.action_copy_link to GoogleMaterial.Icon.gmd_content_copy)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_copy_link -> copyToClipboard(frostWeb.web.url)
            R.id.action_share -> shareText(frostWeb.web.url)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}