package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import com.pitchedapps.frost.R
import com.pitchedapps.frost.SettingsActivity
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-06-30.
 */
fun SettingsActivity.getBehaviourPrefs(): KPrefAdapterBuilder.() -> Unit = {

    checkbox(R.string.fancy_animations, { Prefs.animate }, { Prefs.animate = it; animate = it }) {
        descRes = R.string.fancy_animations_desc
    }

    checkbox(R.string.overlay_full_screen_swipe, { Prefs.overlayFullScreenSwipe }, { Prefs.overlayFullScreenSwipe = it }) {
        descRes = R.string.overlay_full_screen_swipe_desc
    }

    checkbox(R.string.viewpager_swipe, { Prefs.viewpagerSwipe }, { Prefs.viewpagerSwipe = it }) {
        descRes = R.string.viewpager_swipe_desc
    }

    checkbox(R.string.exit_confirmation, { Prefs.exitConfirmation }, { Prefs.exitConfirmation = it }) {
        descRes = R.string.exit_confirmation_desc
    }

    checkbox(R.string.analytics, { Prefs.analytics }, { Prefs.analytics = it }) {
        descRes = R.string.analytics_desc
    }

}
