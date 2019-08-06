/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.launchWebOverlay

/**
 * Created by Allan Wang on 2017-06-30.
 */
fun SettingsActivity.getBehaviourPrefs(): KPrefAdapterBuilder.() -> Unit = {

    checkbox(R.string.auto_refresh_feed, Prefs::autoRefreshFeed, { Prefs.autoRefreshFeed = it }) {
        descRes = R.string.auto_refresh_feed_desc
    }

    checkbox(R.string.fancy_animations, Prefs::animate, { Prefs.animate = it; animate = it }) {
        descRes = R.string.fancy_animations_desc
    }

    checkbox(
        R.string.overlay_swipe,
        Prefs::overlayEnabled,
        { Prefs.overlayEnabled = it; shouldRefreshMain() }) {
        descRes = R.string.overlay_swipe_desc
    }

    checkbox(
        R.string.overlay_full_screen_swipe,
        Prefs::overlayFullScreenSwipe,
        { Prefs.overlayFullScreenSwipe = it }) {
        descRes = R.string.overlay_full_screen_swipe_desc
    }

    checkbox(
        R.string.open_links_in_default,
        Prefs::linksInDefaultApp,
        { Prefs.linksInDefaultApp = it }) {
        descRes = R.string.open_links_in_default_desc
    }

    checkbox(R.string.viewpager_swipe, Prefs::viewpagerSwipe, { Prefs.viewpagerSwipe = it }) {
        descRes = R.string.viewpager_swipe_desc
    }

    checkbox(
        R.string.force_message_bottom,
        Prefs::messageScrollToBottom,
        { Prefs.messageScrollToBottom = it }) {
        descRes = R.string.force_message_bottom_desc
    }

    checkbox(R.string.enable_pip, Prefs::enablePip, { Prefs.enablePip = it }) {
        descRes = R.string.enable_pip_desc
    }

    plainText(R.string.autoplay_settings) {
        descRes = R.string.autoplay_settings_desc
        onClick = {
            launchWebOverlay("${FB_URL_BASE}settings/videos/")
        }
    }

    checkbox(R.string.exit_confirmation, Prefs::exitConfirmation, { Prefs.exitConfirmation = it }) {
        descRes = R.string.exit_confirmation_desc
    }

    checkbox(R.string.analytics, Prefs::analytics, { Prefs.analytics = it }) {
        descRes = R.string.analytics_desc
    }
}
