package com.pitchedapps.frost

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.changelog.showChangelog
import ca.allanwang.kau.kpref.CoreAttributeContract
import ca.allanwang.kau.kpref.KPrefActivity
import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import ca.allanwang.kau.kpref.items.KPrefItemBase
import ca.allanwang.kau.utils.*
import ca.allanwang.kau.views.RippleCanvas
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.settings.*
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.utils.iab.*


/**
 * Created by Allan Wang on 2017-06-06.
 */
class SettingsActivity : KPrefActivity(), IabBroadcastReceiver.IabBroadcastListener {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!(IAB.helper?.handleActivityResult(requestCode, resultCode, data) ?: false))
            super.onActivityResult(requestCode, resultCode, data)
        adapter.notifyDataSetChanged()
    }


    override fun receivedBroadcast() {
        L.d("IAB broadcast")
    }

    override fun kPrefCoreAttributes(): CoreAttributeContract.() -> Unit = {
        textColor = { Prefs.textColor }
        accentColor = { Prefs.accentColor }
    }

    override fun onCreateKPrefs(savedInstanceState: android.os.Bundle?): KPrefAdapterBuilder.() -> Unit = {
        subItems(R.string.appearance, getAppearancePrefs()) {
            descRes = R.string.appearance_desc
            iicon = GoogleMaterial.Icon.gmd_palette
        }

        subItems(R.string.behaviour, getBehaviourPrefs()) {
            descRes = R.string.behaviour_desc
            iicon = GoogleMaterial.Icon.gmd_trending_up
        }

        subItems(R.string.newsfeed, getFeedPrefs()) {
            descRes = R.string.newsfeed_desc
            iicon = CommunityMaterial.Icon.cmd_newspaper
        }

        subItems(R.string.notifications, getNotificationPrefs()) {
            descRes = R.string.notifications_desc
            iicon = GoogleMaterial.Icon.gmd_notifications
        }

        subItems(R.string.experimental, getExperimentalPrefs()) {
            descRes = R.string.experimental_desc
            iicon = CommunityMaterial.Icon.cmd_flask_outline
        }

        plainText(R.string.restore_purchases) {
            descRes = R.string.restore_purchases
            iicon = GoogleMaterial.Icon.gmd_refresh
            onClick = { this@SettingsActivity.restorePurchases(); true }
        }

        plainText(R.string.about_frost) {
            iicon = GoogleMaterial.Icon.gmd_info
            onClick = {
                _, _, _ ->
                startActivity(AboutActivity::class.java, transition = true)
                true
            }
        }

        if (BuildConfig.DEBUG) {
            checkbox(R.string.custom_pro, { Prefs.debugPro }, { Prefs.debugPro = it })
        }
    }

    fun KPrefItemBase.BaseContract<*>.dependsOnPro() {
        onDisabledClick = { _, _, _ -> openPlayProPurchase(0); true }
        enabler = { IS_FROST_PRO }
    }

    fun shouldRestartMain() {
        setResult(MainActivity.REQUEST_RESTART)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setFrostTheme(true)
        super.onCreate(savedInstanceState)
        animate = Prefs.animate
        themeExterior(false)
    }

    fun themeExterior(animate: Boolean = true) {
        if (animate) bgCanvas.fade(Prefs.bgColor)
        else bgCanvas.set(Prefs.bgColor)
        if (animate) toolbarCanvas.ripple(Prefs.headerColor, RippleCanvas.MIDDLE, RippleCanvas.END)
        else toolbarCanvas.set(Prefs.headerColor)
        frostNavigationBar()
    }

    override fun onBackPressed() {
        if (!super.backPress())
            finishSlideOut()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        toolbar.tint(Prefs.iconColor)
        toolbarTitle.textColor = Prefs.iconColor
        toolbarTitle.invalidate()
        setMenuIcons(menu, Prefs.iconColor,
                R.id.action_email to GoogleMaterial.Icon.gmd_email,
                R.id.action_changelog to GoogleMaterial.Icon.gmd_info)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_email -> materialDialogThemed {
                title(R.string.subject)
                items(Support.values().map { string(it.title) })
                itemsCallback {
                    _, _, which, _ ->
                    Support.values()[which].sendEmail(this@SettingsActivity)
                }
            }
            R.id.action_changelog -> showChangelog(R.xml.changelog, Prefs.textColor) { theme() }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}