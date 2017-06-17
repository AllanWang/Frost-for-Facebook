package com.pitchedapps.frost

import android.os.Bundle
import ca.allanwang.kau.kpref.KPrefActivity
import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import ca.allanwang.kau.utils.*
import ca.allanwang.kau.views.RippleCanvas
import com.pitchedapps.frost.utils.*

/**
 * Created by Allan Wang on 2017-06-06.
 */
class SettingsActivity : KPrefActivity() {
    override fun onCreateKPrefs(savedInstanceState: android.os.Bundle?): KPrefAdapterBuilder.() -> Unit = {
        textColor = { Prefs.textColor }
        accentColor = { Prefs.textColor }
        header(R.string.settings)
        text<Int>(R.string.theme, { Prefs.theme }, { Prefs.theme = it }) {
            onClick = {
                _, _, item ->
                this@SettingsActivity.materialDialogThemed {
                    title(R.string.theme)
                    items(Theme.values().map { this@SettingsActivity.string(it.textRes) })
                    itemsDisabledIndices(Theme.CUSTOM.ordinal)
                    itemsCallbackSingleChoice(item.pref, {
                        _, _, which, _ ->
                        if (item.pref != which) {
                            item.pref = which
                            reload()
                            setFrostTheme()
                            themeExterior()
                        }
                        true
                    })
                }
                true
            }
            textGetter = { this@SettingsActivity.string(Theme(it).textRes) }
        }

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

        colorPicker(R.string.icon_color, { Prefs.customIconColor }, { Prefs.customIconColor = it; toolbar.setTitleTextColor(it) }) {
            enabler = { Prefs.isCustomTheme }
            onDisabledClick = { itemView, _, _ -> itemView.snackbar(R.string.requires_custom_theme); true }
            allowCustomAlpha = false
            allowCustom = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        startActivitySlideOut(MainActivity::class.java, clearStack = true, intentBuilder = {
            putParcelableArrayListExtra(EXTRA_COOKIES, cookies())
        })
    }
}