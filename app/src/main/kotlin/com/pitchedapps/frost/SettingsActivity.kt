package com.pitchedapps.frost

import android.os.Bundle
import ca.allanwang.kau.kpref.KPrefActivity
import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import ca.allanwang.kau.utils.*
import ca.allanwang.kau.views.RippleCanvas
import com.pitchedapps.frost.utils.*
import org.jetbrains.anko.toast

/**
 * Created by Allan Wang on 2017-06-06.
 */
class SettingsActivity : KPrefActivity() {

    override fun onCreateKPrefs(savedInstanceState: android.os.Bundle?): KPrefAdapterBuilder.() -> Unit = {
        textColor = { Prefs.textColor }
        accentColor = { Prefs.textColor }
        header(R.string.settings)
        text(R.string.theme, { Prefs.theme }, { Prefs.theme = it }) {
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

        fun Long.timeToText(): String =
                if (this == -1L) string(R.string.none)
                else if (this == 60L) string(R.string.one_hour)
                else if (this == 1440L) string(R.string.one_day)
                else if (this % 1440L == 0L) String.format(string(R.string.x_days), this / 1440L)
                else if (this % 60L == 0L) String.format(string(R.string.x_hours), this / 60L)
                else String.format(string(R.string.x_minutes), this)

        text(R.string.notifications, { Prefs.notificationFreq }, { Prefs.notificationFreq = it; reloadByTitle(R.string.notifications) }) {
            val options = longArrayOf(-1, 15, 30, 60, 120, 180, 300, 1440, 2880)
            val texts = options.map { it.timeToText() }
            onClick = {
                _, _, item ->
                this@SettingsActivity.materialDialogThemed {
                    title(R.string.notifications)
                    items(texts)
                    itemsCallbackSingleChoice(options.indexOf(item.pref), {
                        _, _, which, text ->
                        item.pref = options[which]
                        this@SettingsActivity.scheduleNotifications(item.pref)
                        this@SettingsActivity.toast(text)
                        true
                    })
                }
                true
            }
            textGetter = { it.timeToText() }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFrostTheme()
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