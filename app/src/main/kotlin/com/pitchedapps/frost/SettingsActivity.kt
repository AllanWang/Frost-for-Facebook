package com.pitchedapps.frost

import android.os.Bundle
import ca.allanwang.kau.kpref.KPrefActivity
import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import ca.allanwang.kau.utils.*
import ca.allanwang.kau.views.RippleCanvas
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.utils.EXTRA_COOKIES
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.Theme
import com.pitchedapps.frost.utils.cookies

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
                itemView, innerContent, item ->
                this@SettingsActivity.materialDialog {
                    title(R.string.theme)
                    items(Theme.values().map { this@SettingsActivity.string(it.textRes) })
                    itemsCallbackSingleChoice(item.pref, {
                        _, _, which, _ ->
                        if (item.pref != which) {
                            item.pref = which
                            reload()
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
            getter = { Prefs.textColor }
            setter = { Prefs.textColor = it; reload() }
        }, colorBuilder = {
            allowCustom = true
        })

        colorPicker(title = R.string.background_color, coreBuilder = {
            iicon = GoogleMaterial.Icon.gmd_colorize
        }, itemBuilder = {
            getter = { Prefs.bgColor }
            setter = { Prefs.bgColor = it; bgCanvas.ripple(it, duration = 500L) }
        }, colorBuilder = {
            allowCustomAlpha = false
            allowCustom = true
        })

        colorPicker(title = R.string.header_color, itemBuilder = {
            getter = { Prefs.headerColor }
            setter = {
                Prefs.headerColor = it
                val darkerColor = it.darken()
                this@SettingsActivity.navigationBarColor = darkerColor
                toolbarCanvas.ripple(darkerColor, RippleCanvas.MIDDLE, RippleCanvas.END, duration = 500L)
            }
        }, colorBuilder = {
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

    override fun onBackPressed() {
        startActivitySlideOut(MainActivity::class.java, clearStack = true, intentBuilder = {
            putParcelableArrayListExtra(EXTRA_COOKIES, cookies())
        })
    }
}