package com.pitchedapps.frost.settings

import android.content.Context
import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import com.afollestad.materialdialogs.MaterialDialog
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.injectors.JsInjector
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.materialDialogThemed

/**
 * Created by Allan Wang on 2017-06-30.
 */
fun SettingsActivity.getDebugPrefs(context: Context): KPrefAdapterBuilder.() -> Unit = {

    plainText(R.string.experimental_disclaimer) {
        descRes = R.string.debug_disclaimer_info
    }

    checkbox(R.string.fancy_animations, { Prefs.animate }, { Prefs.animate = it; animate = it }) {
        descRes = R.string.fancy_animations_desc
    }

}

/**
 * Prepare our debug report
 */
private fun Context.loadDebugger(url: String, injector: JsInjector? = null) {
    val dialog = materialDialogThemed {

    }
}

private fun MaterialDialog