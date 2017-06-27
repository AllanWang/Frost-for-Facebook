package com.pitchedapps.frost

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.changelog.showChangelog
import ca.allanwang.kau.email.sendEmail
import ca.allanwang.kau.kpref.CoreAttributeContract
import ca.allanwang.kau.kpref.KPrefActivity
import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import ca.allanwang.kau.kpref.items.KPrefColorPicker
import ca.allanwang.kau.kpref.items.KPrefItemBase
import ca.allanwang.kau.utils.*
import ca.allanwang.kau.views.RippleCanvas
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.facebook.FeedSort
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.utils.iab.IS_FROST_PRO
import com.pitchedapps.frost.utils.iab.openPlayProPurchase
import com.pitchedapps.frost.views.Keywords


/**
 * Created by Allan Wang on 2017-06-06.
 */
class SettingsActivity : KPrefActivity() {

    override fun kPrefCoreAttributes(): CoreAttributeContract.() -> Unit = {
        textColor = { Prefs.textColor }
        accentColor = { Prefs.accentColor }
    }

    override fun onCreateKPrefs(savedInstanceState: android.os.Bundle?): KPrefAdapterBuilder.() -> Unit = {
        subItems(R.string.appearance, subPrefsAppearance()) {
            descRes = R.string.appearance_desc
            iicon = GoogleMaterial.Icon.gmd_palette
        }

        subItems(R.string.newsfeed, subPrefsFeed()) {
            descRes = R.string.newsfeed_desc
            iicon = CommunityMaterial.Icon.cmd_newspaper
        }

        subItems(R.string.notifications, subPrefsNotifications()) {
            descRes = R.string.notifications_desc
            iicon = GoogleMaterial.Icon.gmd_notifications
        }

        plainText(R.string.about_frost) {
            onClick = {
                _, _, _ ->
                LibsBuilder()
                        //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        //start the activity
                        .start(this@SettingsActivity)
                true
            }
        }


        if (BuildConfig.DEBUG) {
            checkbox(R.string.custom_pro, { Prefs.debugPro }, { Prefs.debugPro = it })
        }
    }

    fun subPrefsAppearance(): KPrefAdapterBuilder.() -> Unit = {

        header(R.string.theme_customization)

        text(R.string.theme, { Prefs.theme }, { Prefs.theme = it }) {
            onClick = {
                _, _, item ->
                this@SettingsActivity.materialDialogThemed {
                    title(R.string.theme)
                    items(Theme.values()
                            .map { if (it == Theme.CUSTOM && !IS_FROST_PRO) R.string.custom_pro else it.textRes }
                            .map { this@SettingsActivity.string(it) })
                    itemsCallbackSingleChoice(item.pref, {
                        _, _, which, text ->
                        if (item.pref != which) {
                            if (which == Theme.CUSTOM.ordinal && !IS_FROST_PRO) {
                                this@SettingsActivity.openPlayProPurchase(9)
                                return@itemsCallbackSingleChoice true
                            }
                            item.pref = which
                            shouldRestartMain()
                            reload()
                            setFrostTheme(true)
                            themeExterior()
                            invalidateOptionsMenu()
                            frostAnswersCustom("Theme") { putCustomAttribute("Count", text.toString()) }
                        }
                        true
                    })
                }
                true
            }
            textGetter = {
                this@SettingsActivity.string(Theme(it).textRes)
            }
        }

        fun KPrefColorPicker.KPrefColorContract.dependsOnCustom() {
            enabler = { Prefs.isCustomTheme }
            onDisabledClick = { itemView, _, _ -> itemView.frostSnackbar(R.string.requires_custom_theme); true }
            allowCustom = true
        }

        fun invalidateCustomTheme() {
            CssAssets.CUSTOM.injector = null
        }

        colorPicker(R.string.text_color, { Prefs.customTextColor }, {
            Prefs.customTextColor = it
            reload()
            invalidateCustomTheme()
            shouldRestartMain()
        }) {
            dependsOnCustom()
            allowCustomAlpha = false
        }

        colorPicker(R.string.background_color, { Prefs.customBackgroundColor }, {
            Prefs.customBackgroundColor = it
            bgCanvas.ripple(it, duration = 500L)
            invalidateCustomTheme()
            setFrostTheme(true)
            shouldRestartMain()
        }) {
            dependsOnCustom()
            allowCustomAlpha = true
        }

        colorPicker(R.string.header_color, { Prefs.customHeaderColor }, {
            Prefs.customHeaderColor = it
            if (Prefs.tintNavBar) this@SettingsActivity.frostNavigationBar()
            toolbarCanvas.ripple(it, RippleCanvas.MIDDLE, RippleCanvas.END, duration = 500L)
            reload()
            shouldRestartMain()
        }) {
            dependsOnCustom()
            allowCustomAlpha = true
        }

        colorPicker(R.string.icon_color, { Prefs.customIconColor }, {
            Prefs.customIconColor = it
            invalidateOptionsMenu()
            shouldRestartMain()
        }) {
            dependsOnCustom()
            allowCustomAlpha = false
        }

        header(R.string.global_customization)

        checkbox(R.string.rounded_icons, { Prefs.showRoundedIcons }, {
            Prefs.showRoundedIcons = it
            setResult(MainActivity.REQUEST_REFRESH)
        }) {
            descRes = R.string.rounded_icons_desc
        }

        checkbox(R.string.fancy_animations, { Prefs.animate }, { Prefs.animate = it; animate = it }) {
            descRes = R.string.fancy_animations_desc
        }

        checkbox(R.string.tint_nav, { Prefs.tintNavBar }, {
            Prefs.tintNavBar = it
            this@SettingsActivity.frostNavigationBar()
            setResult(MainActivity.REQUEST_NAV)
        }) {
            descRes = R.string.tint_nav_desc
        }
    }

    fun KPrefItemBase.BaseContract<*>.dependsOnPro() {
        onDisabledClick = { _, _, _ -> openPlayProPurchase(0); true }
        enabler = { IS_FROST_PRO }
    }

    fun subPrefsFeed(): KPrefAdapterBuilder.() -> Unit = {

        text(R.string.newsfeed_sort, { Prefs.feedSort }, { Prefs.feedSort = it }) {
            descRes = R.string.newsfeed_sort_desc
            onClick = {
                _, _, item ->
                this@SettingsActivity.materialDialogThemed {
                    title(R.string.newsfeed_sort)
                    items(FeedSort.values().map { this@SettingsActivity.string(it.textRes) })
                    itemsCallbackSingleChoice(item.pref, {
                        _, _, which, text ->
                        if (item.pref != which) {
                            item.pref = which
                            shouldRestartMain()
                        }
                        true
                    })
                }
                true
            }
            textGetter = { string(FeedSort(it).textRes) }
        }

        checkbox(R.string.suggested_friends, { Prefs.showSuggestedFriends }, {
            Prefs.showSuggestedFriends = it
            setResult(MainActivity.REQUEST_REFRESH)
        }) {
            descRes = R.string.suggested_friends_desc
            dependsOnPro()
        }

        checkbox(R.string.facebook_ads, { Prefs.showFacebookAds }, {
            Prefs.showFacebookAds = it
            setResult(MainActivity.REQUEST_REFRESH)
        }) {
            descRes = R.string.facebook_ads_desc
            dependsOnPro()
        }
    }

    fun subPrefsNotifications(): KPrefAdapterBuilder.() -> Unit = {

        text(R.string.notification_frequency, { Prefs.notificationFreq }, { Prefs.notificationFreq = it }) {
            val options = longArrayOf(-1, 15, 30, 60, 120, 180, 300, 1440, 2880)
            val texts = options.map { this@SettingsActivity.minuteToText(it) }
            onClick = {
                _, _, item ->
                this@SettingsActivity.materialDialogThemed {
                    title(R.string.notification_frequency)
                    items(texts)
                    itemsCallbackSingleChoice(options.indexOf(item.pref), {
                        _, _, which, _ ->
                        item.pref = options[which]
                        this@SettingsActivity.scheduleNotifications(item.pref)
                        true
                    })
                }
                true
            }
            textGetter = { this@SettingsActivity.minuteToText(it) }
        }

        plainText(R.string.notification_keywords) {
            descRes = R.string.notification_keywords_desc
            onClick = {
                _, _, _ ->
                val keywordView = Keywords(this@SettingsActivity)
                this@SettingsActivity.materialDialogThemed {
                    title(R.string.notification_keywords)
                    customView(keywordView, false)
                    dismissListener { keywordView.save() }
                    positiveText(R.string.kau_done)
                }
                true
            }
        }

    }

    fun shouldRestartMain() {
        setResult(MainActivity.REQUEST_RESTART)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setFrostTheme(true)
        animate = Prefs.animate
        super.onCreate(savedInstanceState)
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
            R.id.action_email -> sendEmail(R.string.dev_email, R.string.frost_feedback) {
                addItem("Random Frost ID", Prefs.frostId)
            }
            R.id.action_changelog -> showChangelog(R.xml.changelog, Prefs.textColor) { theme() }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}