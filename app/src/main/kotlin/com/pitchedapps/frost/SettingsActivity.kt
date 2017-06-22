package com.pitchedapps.frost

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.email.sendEmail
import ca.allanwang.kau.kpref.CoreAttributeContract
import ca.allanwang.kau.kpref.KPrefActivity
import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import ca.allanwang.kau.utils.*
import ca.allanwang.kau.views.RippleCanvas
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.views.Keywords


/**
 * Created by Allan Wang on 2017-06-06.
 */
class SettingsActivity : KPrefActivity() {

    override fun kPrefCoreAttributes(): CoreAttributeContract.() -> Unit = {
        textColor = { Prefs.textColor }
        accentColor = {
            if (Prefs.headerColor.isColorVisibleOn(Prefs.bgColor, 100)) Prefs.headerColor
            else Prefs.textColor
        }
    }

    override fun onCreateKPrefs(savedInstanceState: android.os.Bundle?): KPrefAdapterBuilder.() -> Unit = {
        subItems(R.string.appearance, subPrefsAppearance()) {
            descRes = R.string.appearance_desc
            iicon = GoogleMaterial.Icon.gmd_palette
        }
        subItems(R.string.notifications, subPrefsNotifications()) {
            descRes = R.string.notifications_desc
            iicon = GoogleMaterial.Icon.gmd_notifications
        }
    }

    fun subPrefsAppearance(): KPrefAdapterBuilder.() -> Unit = {

        header(R.string.base_customization)

        text(R.string.theme, { Prefs.theme }, { Prefs.theme = it }) {
            onClick = {
                _, _, item ->
                this@SettingsActivity.materialDialogThemed {
                    title(R.string.theme)
                    items(Theme.values()
                            .filter { it != Theme.CUSTOM || BuildConfig.DEBUG } //TODO actually add custom theme
                            .map { this@SettingsActivity.string(it.textRes) })
//                    itemsDisabledIndices(Theme.CUSTOM.ordinal)
                    itemsCallbackSingleChoice(item.pref, {
                        _, _, which, text ->
                        if (item.pref != which) {
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
            textGetter = { this@SettingsActivity.string(Theme(it).textRes) }
        }

        if (BuildConfig.DEBUG) {
            colorPicker(R.string.text_color, { Prefs.customTextColor }, { Prefs.customTextColor = it; reload() }) {
                enabler = { Prefs.isCustomTheme }
                onDisabledClick = { itemView, _, _ -> itemView.snackbar(R.string.requires_custom_theme); true }
                allowCustomAlpha = false
                allowCustom = true
            }

            colorPicker(R.string.background_color, { Prefs.customBackgroundColor },
                    { Prefs.customBackgroundColor = it; bgCanvas.ripple(it, duration = 500L) }) {
                enabler = { Prefs.isCustomTheme }
                onDisabledClick = { itemView, _, _ -> itemView.snackbar(R.string.requires_custom_theme); true }
                allowCustomAlpha = true
                allowCustom = true
            }

            colorPicker(R.string.header_color, { Prefs.customHeaderColor }, {
                Prefs.customHeaderColor = it
                val darkerColor = it.darken()
                this@SettingsActivity.navigationBarColor = darkerColor
                toolbarCanvas.ripple(darkerColor, RippleCanvas.MIDDLE, RippleCanvas.END, duration = 500L)
            }) {
                enabler = { Prefs.isCustomTheme }
                onDisabledClick = { itemView, _, _ -> itemView.snackbar(R.string.requires_custom_theme); true }
                allowCustomAlpha = true
                allowCustom = true
            }

            colorPicker(R.string.icon_color, { Prefs.customIconColor }, {
                Prefs.customIconColor = it
                invalidateOptionsMenu()
            }) {
                enabler = { Prefs.isCustomTheme }
                onDisabledClick = { itemView, _, _ -> itemView.snackbar(R.string.requires_custom_theme); true }
                allowCustom = true
            }

            checkbox(R.string.rounded_icons, { Prefs.showRoundedIcons }, { Prefs.showRoundedIcons = it })

            checkbox(R.string.fancy_animations, { Prefs.animate }, { Prefs.animate = it; animate = it }) {
                descRes = R.string.fancy_animations_desc
            }

            header(R.string.feed_customization)

            checkbox(R.string.suggested_friends, { Prefs.showSuggestedFriends }, { Prefs.showSuggestedFriends = it }) {
                descRes = R.string.suggested_friends_desc
            }

            checkbox(R.string.facebook_ads, { Prefs.showFacebookAds }, { Prefs.showFacebookAds = it }) {
                descRes = R.string.facebook_ads_desc
            }
        }


    }

    fun subPrefsNotifications(): KPrefAdapterBuilder.() -> Unit = {

        text(R.string.notification_frequency, { Prefs.notificationFreq }, { Prefs.notificationFreq = it; reloadByTitle(R.string.notifications) }) {
            val options = longArrayOf(-1, 15, 30, 60, 120, 180, 300, 1440, 2880)
            val texts = options.map { this@SettingsActivity.minuteToText(it) }
            onClick = {
                _, _, item ->
                this@SettingsActivity.materialDialogThemed {
                    title(R.string.notification_frequency)
                    items(texts)
                    itemsCallbackSingleChoice(options.indexOf(item.pref), {
                        _, _, which, text ->
                        item.pref = options[which]
                        this@SettingsActivity.scheduleNotifications(item.pref)
                        true
                    })
                }
                true
            }
            textGetter = { this@SettingsActivity.minuteToText(it) }
        }

        text<String?>(R.string.notification_keywords, { null }, { }) {
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
        val darkAccent = Prefs.headerColor.darken()
        if (animate) toolbarCanvas.ripple(darkAccent, RippleCanvas.MIDDLE, RippleCanvas.END)
        else toolbarCanvas.set(darkAccent)
        this.navigationBarColor = darkAccent
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
                addItem("Random Frost ID", "${Prefs.installDate}-${Prefs.identifier}")
            }
            R.id.action_changelog -> showChangelog(R.xml.changelog, { theme() })
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}