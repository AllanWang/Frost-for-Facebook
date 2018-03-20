package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.kpref.activity.items.KPrefColorPicker
import ca.allanwang.kau.kpref.activity.items.KPrefSeekbar
import ca.allanwang.kau.ui.views.RippleCanvas
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.enums.MainActivityLayout
import com.pitchedapps.frost.enums.Theme
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.utils.iab.IS_FROST_PRO
import com.pitchedapps.frost.views.KPrefTextSeekbar

/**
 * Created by Allan Wang on 2017-06-29.
 */
fun SettingsActivity.getAppearancePrefs(): KPrefAdapterBuilder.() -> Unit = {

    header(R.string.theme_customization)

    text(R.string.theme, Prefs::theme, { Prefs.theme = it }) {
        onClick = {
            materialDialogThemed {
                title(R.string.theme)
                items(Theme.values()
                        .map { if (it == Theme.CUSTOM && !IS_FROST_PRO) R.string.custom_pro else it.textRes }
                        .map { string(it) })
                itemsCallbackSingleChoice(item.pref) { _, _, which, _ ->
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
                        frostEvent("Theme", "Count" to Theme(which).name)
                    }
                    true
                }
            }
        }
        textGetter = {
            string(Theme(it).textRes)
        }
    }

    fun KPrefColorPicker.KPrefColorContract.dependsOnCustom() {
        enabler = Prefs::isCustomTheme
        onDisabledClick = { frostSnackbar(R.string.requires_custom_theme) }
        allowCustom = true
    }

    fun invalidateCustomTheme() {
        CssAssets.CUSTOM.injector.invalidate()
    }

    colorPicker(R.string.text_color, Prefs::customTextColor, {
        Prefs.customTextColor = it
        reload()
        invalidateCustomTheme()
        shouldRestartMain()
    }) {
        dependsOnCustom()
        allowCustomAlpha = false
    }

    colorPicker(R.string.accent_color, Prefs::customAccentColor, {
        Prefs.customAccentColor = it
        reload()
        invalidateCustomTheme()
        shouldRestartMain()
    }) {
        dependsOnCustom()
        allowCustomAlpha = false
    }


    colorPicker(R.string.background_color, Prefs::customBackgroundColor, {
        Prefs.customBackgroundColor = it
        bgCanvas.ripple(it, duration = 500L)
        invalidateCustomTheme()
        setFrostTheme(true)
        shouldRestartMain()
    }) {
        dependsOnCustom()
        allowCustomAlpha = true
    }

    colorPicker(R.string.header_color, Prefs::customHeaderColor, {
        Prefs.customHeaderColor = it
        frostNavigationBar()
        toolbarCanvas.ripple(it, RippleCanvas.MIDDLE, RippleCanvas.END, duration = 500L)
        reload()
        shouldRestartMain()
    }) {
        dependsOnCustom()
        allowCustomAlpha = true
    }

    colorPicker(R.string.icon_color, Prefs::customIconColor, {
        Prefs.customIconColor = it
        invalidateOptionsMenu()
        shouldRestartMain()
    }) {
        dependsOnCustom()
        allowCustomAlpha = false
    }

    header(R.string.global_customization)

    text(R.string.main_activity_layout, Prefs::mainActivityLayoutType, { Prefs.mainActivityLayoutType = it }) {
        textGetter = { string(Prefs.mainActivityLayout.titleRes) }
        onClick = {
            materialDialogThemed {
                title(R.string.main_activity_layout_desc)
                items(MainActivityLayout.values.map { string(it.titleRes) })
                itemsCallbackSingleChoice(item.pref) { _, _, which, _ ->
                    if (item.pref != which) {
                        item.pref = which
                        shouldRestartMain()
                        frostEvent("Main Layout", "Type" to MainActivityLayout(which).name)
                    }
                    true
                }
            }
        }
    }

    plainText(R.string.main_tabs) {
        descRes = R.string.main_tabs_desc
        onClick = { launchTabCustomizerActivity() }
    }

    checkbox(R.string.rounded_icons, Prefs::showRoundedIcons, {
        Prefs.showRoundedIcons = it
        setFrostResult(REQUEST_REFRESH)
    }) {
        descRes = R.string.rounded_icons_desc
    }

    checkbox(R.string.tint_nav, Prefs::tintNavBar, {
        Prefs.tintNavBar = it
        frostNavigationBar()
        setFrostResult(REQUEST_NAV)
    }) {
        descRes = R.string.tint_nav_desc
    }

    list.add(KPrefTextSeekbar(
            KPrefSeekbar.KPrefSeekbarBuilder(
                    globalOptions,
                    R.string.web_text_scaling, Prefs::webTextScaling, { Prefs.webTextScaling = it; setFrostResult(REQUEST_TEXT_ZOOM) })))

    checkbox(R.string.enforce_black_media_bg, Prefs::blackMediaBg, {
        Prefs.blackMediaBg = it
    }) {
        descRes = R.string.enforce_black_media_bg_desc
    }
}