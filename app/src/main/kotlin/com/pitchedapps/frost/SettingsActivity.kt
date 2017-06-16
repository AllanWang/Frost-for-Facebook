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
        text<Int>(title = R.string.theme, itemBuilder = {
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
                            setFrostTheme(true)
                            themeExterior()
                        }
                        true
                    })
                }
                true
            }
            getter = { Prefs.theme }
            setter = { Prefs.theme = it }
        }, textGetter = { this@SettingsActivity.string(Theme(it).textRes) })
        colorPicker(title = R.string.text_color, itemBuilder = {
            getter = { Prefs.customTextColor }
            setter = { Prefs.customTextColor = it; reload() }
            enabler = { Prefs.isCustomTheme }
            onDisabledClick = { itemView, _, _ ->
                itemView.snackbar(R.string.requires_custom_theme)
                true
            }
        }, colorBuilder = {
            allowCustomAlpha = false
            allowCustom = true
        })

        colorPicker(title = R.string.background_color, itemBuilder = {
            getter = { Prefs.customBackgroundColor }
            setter = { Prefs.customBackgroundColor = it; bgCanvas.ripple(it, duration = 500L) }
            enabler = { Prefs.isCustomTheme }
            onDisabledClick = { itemView, _, _ ->
                itemView.snackbar(R.string.requires_custom_theme)
                true
            }
        }, colorBuilder = {
            allowCustomAlpha = true
            allowCustom = true
        })

        colorPicker(title = R.string.header_color, itemBuilder = {
            getter = { Prefs.customHeaderColor }
            setter = {
                Prefs.customHeaderColor = it
                val darkerColor = it.darken()
                this@SettingsActivity.navigationBarColor = darkerColor
                toolbarCanvas.ripple(darkerColor, RippleCanvas.MIDDLE, RippleCanvas.END, duration = 500L)
            }
            enabler = { Prefs.isCustomTheme }
            onDisabledClick = { itemView, _, _ ->
                itemView.snackbar(R.string.requires_custom_theme)
                true
            }
        }, colorBuilder = {
            allowCustomAlpha = true
            allowCustom = true
        })

        colorPicker(title = R.string.icon_color, itemBuilder = {
            getter = { Prefs.customIconColor }
            setter = { Prefs.customIconColor = it; toolbar.setTitleTextColor(it) }
            enabler = { Prefs.isCustomTheme }
            onDisabledClick = { itemView, _, _ ->
                itemView.snackbar(R.string.requires_custom_theme)
                true
            }
        }, colorBuilder = {
            allowCustomAlpha = false
            allowCustom = true
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeExterior(false)
        setFrostTheme(true)
    }

    fun themeExterior(animate: Boolean = true) {
        if (animate) bgCanvas.fade(Prefs.bgColor)
        else bgCanvas.set(Prefs.bgColor)
        val darkAccent = Prefs.headerColor.darken()
        if (animate) toolbarCanvas.ripple(darkAccent, RippleCanvas.MIDDLE, RippleCanvas.END)
        else toolbarCanvas.set(darkAccent)
        this.navigationBarColor = darkAccent
    }

    private fun relativeDuration(canvas: RippleCanvas): Long = Math.hypot(canvas.height.toDouble(), canvas.width.toDouble() / 2).toLong()

    override fun onBackPressed() {
        startActivitySlideOut(MainActivity::class.java, clearStack = true, intentBuilder = {
            putParcelableArrayListExtra(EXTRA_COOKIES, cookies())
        })
    }
}