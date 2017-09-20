package com.pitchedapps.frost.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.kpref.activity.CoreAttributeContract
import ca.allanwang.kau.kpref.activity.KPrefActivity
import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.kpref.activity.items.KPrefItemBase
import ca.allanwang.kau.ui.views.RippleCanvas
import ca.allanwang.kau.utils.*
import ca.allanwang.kau.xml.showChangelog
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.enums.Support
import com.pitchedapps.frost.settings.*
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.utils.iab.FrostBilling
import com.pitchedapps.frost.utils.iab.IABSettings
import com.pitchedapps.frost.utils.iab.IS_FROST_PRO


/**
 * Created by Allan Wang on 2017-06-06.
 */
class SettingsActivity : KPrefActivity(), FrostBilling by IABSettings() {

    var resultFlag = Activity.RESULT_CANCELED

    companion object {
        private const val REQUEST_RINGTONE = 0b10111 shl 5
        const val REQUEST_NOTIFICATION_RINGTONE = REQUEST_RINGTONE or 1
        const val REQUEST_MESSAGE_RINGTONE = REQUEST_RINGTONE or 2
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (fetchRingtone(requestCode, resultCode, data)) return
        if (!onActivityResultBilling(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data)
        reloadList()
    }

    /**
     * Fetch ringtone and save uri
     * Returns [true] if consumed, [false] otherwise
     */
    private fun fetchRingtone(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode and REQUEST_RINGTONE != REQUEST_RINGTONE || resultCode != Activity.RESULT_OK) return false
        val uri: String = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.toString() ?: ""
        when (requestCode) {
            REQUEST_NOTIFICATION_RINGTONE -> {
                Prefs.notificationRingtone = uri
                reloadByTitle(R.string.notification_ringtone)
            }
            REQUEST_MESSAGE_RINGTONE -> {
                Prefs.messageRingtone = uri
                reloadByTitle(R.string.message_ringtone)
            }
        }
        return true
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

//        subItems(R.string.network, getNetworkPrefs()) {
//            descRes = R.string.network_desc
//            iicon = GoogleMaterial.Icon.gmd_network_cell
//        }

        subItems(R.string.experimental, getExperimentalPrefs()) {
            descRes = R.string.experimental_desc
            iicon = CommunityMaterial.Icon.cmd_flask_outline
        }

        plainText(R.string.get_pro) {
            descRes = R.string.get_pro_desc
            iicon = GoogleMaterial.Icon.gmd_star
            onClick = { _, _, _ -> restorePurchases(); true }
        }

        plainText(R.string.about_frost) {
            descRes = R.string.about_frost_desc
            iicon = GoogleMaterial.Icon.gmd_info
            onClick = { _, _, _ -> startActivityForResult(AboutActivity::class.java, 9, true); true }
        }

        plainText(R.string.replay_intro) {
            iicon = GoogleMaterial.Icon.gmd_replay
            onClick = { _, _, _ -> launchIntroActivity(cookies()); true }
        }

        subItems(R.string.debug_frost, getDebugPrefs()) {
            descRes = R.string.debug_frost_desc
            iicon = CommunityMaterial.Icon.cmd_android_debug_bridge
            visible = { Prefs.debugSettings }
        }

        if (BuildConfig.DEBUG) {
            checkbox(R.string.custom_pro, { Prefs.debugPro }, { Prefs.debugPro = it })
        }
    }

    fun KPrefItemBase.BaseContract<*>.dependsOnPro() {
        onDisabledClick = { _, _, _ -> purchasePro(); true }
        enabler = { IS_FROST_PRO }
    }

    fun shouldRestartMain() {
        setFrostResult(MainActivity.REQUEST_RESTART)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        setFrostTheme(true)
        super.onCreate(savedInstanceState)
        animate = Prefs.animate
        themeExterior(false)
        onCreateBilling()
    }

    fun themeExterior(animate: Boolean = true) {
        if (animate) bgCanvas.fade(Prefs.bgColor)
        else bgCanvas.set(Prefs.bgColor)
        if (animate) toolbarCanvas.ripple(Prefs.headerColor, RippleCanvas.MIDDLE, RippleCanvas.END)
        else toolbarCanvas.set(Prefs.headerColor)
        frostNavigationBar()
    }

    override fun onBackPressed() {
        if (!super.backPress()) {
            setResult(resultFlag)
            finishSlideOut()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        toolbar.tint(Prefs.iconColor)
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
                itemsCallback { _, _, which, _ -> Support.values()[which].sendEmail(this@SettingsActivity) }
            }
            R.id.action_changelog -> showChangelog(R.xml.frost_changelog, Prefs.textColor) { theme() }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun setFrostResult(flag: Int) {
        resultFlag = resultFlag or flag
    }

    override fun onDestroy() {
        onDestroyBilling()
        super.onDestroy()
    }
}