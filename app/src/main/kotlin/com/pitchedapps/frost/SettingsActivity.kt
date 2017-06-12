package com.pitchedapps.frost

import android.os.Bundle
import ca.allanwang.kau.kpref.KPrefActivity
import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import ca.allanwang.kau.utils.darken
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.views.RippleCanvas
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-06-06.
 */
class SettingsActivity : KPrefActivity() {
    override fun onCreateKPrefs(savedInstanceState: android.os.Bundle?): KPrefAdapterBuilder.() -> Unit = {
        textColor = { Prefs.textColor }
        accentColor = { Prefs.textColor }
        header(R.string.settings)
        colorPicker(title = R.string.text_color,
                getter = { Prefs.textColor }, setter = { Prefs.textColor = it; reload() },
                configs = {
                    allowCustom = true
                })
        colorPicker(iicon = GoogleMaterial.Icon.gmd_colorize,
                title = R.string.background_color,
                getter = { Prefs.bgColor }, setter = { Prefs.bgColor = it; bgCanvas.ripple(it, duration = 500L) },
                configs = {
                    allowCustomAlpha = false
                    allowCustom = true
                })
        colorPicker(title = R.string.header_color,
                getter = { Prefs.headerColor }, setter = {
            Prefs.headerColor = it
            val darkerColor = it.darken()
            this@SettingsActivity.navigationBarColor = darkerColor
            toolbarCanvas.ripple(darkerColor, RippleCanvas.MIDDLE, RippleCanvas.END, duration = 500L)
        },
                configs = {
                    allowCustom = false
                })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bgCanvas.set(Prefs.bgColor)
        val darkAccent = Prefs.headerColor.darken()
        toolbarCanvas.set(darkAccent)
        this.navigationBarColor = darkAccent
    }
}