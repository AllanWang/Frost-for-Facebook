package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.kpref.activity.items.KPrefColorPicker
import ca.allanwang.kau.kpref.activity.items.KPrefSeekbar
import ca.allanwang.kau.ui.views.RippleCanvas
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.utils.iab.IS_FROST_PRO
import com.pitchedapps.frost.views.KPrefTextSeekbar

/**
 * Created by Allan Wang on 2017-06-29.
 */
fun SettingsActivity.getAppearancePrefs(): KPrefAdapterBuilder.() -> Unit = {

    header(R.string.theme_customization)

    text(R.string.theme, { Prefs.theme }, { Prefs.theme = it }) {
        onClick = {
            _, _, item ->
            materialDialogThemed {
                title(R.string.theme)
                items(Theme.values()
                        .map { if (it == Theme.CUSTOM && !IS_FROST_PRO) R.string.custom_pro else it.textRes }
                        .map { context.string(it) })
                itemsCallbackSingleChoice(item.pref) {
                    _, _, which, text ->
                    if (item.pref != which) {
                        if (which == Theme.CUSTOM.ordinal && !IS_FROST_PRO) {
                            purchasePro()
                            return@itemsCallbackSingleChoice true
                        }
                        item.pref = which
                        shouldRestartMain()
                        reload()
                        setFrostTheme(true)
                        themeExterior()
                        invalidateOptionsMenu()
                        frostAnswersCustom("Theme", "Count" to text)
                    }
                    true
                }
            }
            true
        }
        textGetter = {
            string(Theme(it).textRes)
        }
    }

    fun KPrefColorPicker.KPrefColorContract.dependsOnCustom() {
        enabler = { Prefs.isCustomTheme }
        onDisabledClick = { itemView, _, _ -> frostSnackbar(R.string.requires_custom_theme); true }
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

    colorPicker(R.string.accent_color, { Prefs.customAccentColor }, {
        Prefs.customAccentColor = it
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
        frostNavigationBar()
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
        setFrostResult(MainActivity.REQUEST_REFRESH)
    }) {
        descRes = R.string.rounded_icons_desc
    }

    checkbox(R.string.tint_nav, { Prefs.tintNavBar }, {
        Prefs.tintNavBar = it
        frostNavigationBar()
        setFrostResult(MainActivity.REQUEST_NAV)
    }) {
        descRes = R.string.tint_nav_desc
    }

    list.add(KPrefTextSeekbar(
            KPrefSeekbar.KPrefSeekbarBuilder(
                    globalOptions,
                    R.string.web_text_scaling, { Prefs.webTextScaling }, { Prefs.webTextScaling = it; setFrostResult(MainActivity.REQUEST_WEB_ZOOM) })))
}