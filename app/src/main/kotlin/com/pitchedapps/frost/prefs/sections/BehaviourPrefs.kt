/*
 * Copyright 2020 Allan Wang
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
package com.pitchedapps.frost.prefs.sections

import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.KPrefFactory
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.prefs.OldPrefs
import com.pitchedapps.frost.prefs.PrefsBase
import org.koin.core.KoinComponent
import org.koin.core.inject

interface BehaviourPrefs : PrefsBase {
    var biometricsEnabled: Boolean

    var overlayEnabled: Boolean

    var overlayFullScreenSwipe: Boolean

    var viewpagerSwipe: Boolean

    var loadMediaOnMeteredNetwork: Boolean

    var debugSettings: Boolean

    var linksInDefaultApp: Boolean

    var blackMediaBg: Boolean

    var autoRefreshFeed: Boolean

    var showCreateFab: Boolean

    var fullSizeImage: Boolean

    var autoExpandTextBox: Boolean
}

class BehaviourPrefsImpl(
    factory: KPrefFactory
) : KPref("${BuildConfig.APPLICATION_ID}.prefs.behaviour", factory),
    BehaviourPrefs, KoinComponent {

    private val oldPrefs: OldPrefs by inject()

    override var biometricsEnabled: Boolean by kpref(
        "biometrics_enabled",
        oldPrefs.biometricsEnabled /* false */
    )

    override var overlayEnabled: Boolean by kpref(
        "overlay_enabled",
        oldPrefs.overlayEnabled /* true */
    )

    override var overlayFullScreenSwipe: Boolean by kpref(
        "overlay_full_screen_swipe",
        oldPrefs.overlayFullScreenSwipe /* true */
    )

    override var viewpagerSwipe: Boolean by kpref(
        "viewpager_swipe",
        oldPrefs.viewpagerSwipe /* true */
    )

    override var loadMediaOnMeteredNetwork: Boolean by kpref(
        "media_on_metered_network",
        oldPrefs.loadMediaOnMeteredNetwork /* true */
    )

    override var debugSettings: Boolean by kpref(
        "debug_settings",
        oldPrefs.debugSettings /* false */
    )

    override var linksInDefaultApp: Boolean by kpref(
        "link_in_default_app",
        oldPrefs.linksInDefaultApp /* false */
    )

    override var blackMediaBg: Boolean by kpref("black_media_bg", oldPrefs.blackMediaBg /* false */)

    override var autoRefreshFeed: Boolean by kpref(
        "auto_refresh_feed",
        oldPrefs.autoRefreshFeed /* false */
    )

    override var showCreateFab: Boolean by kpref(
        "show_create_fab",
        oldPrefs.showCreateFab /* true */
    )

    override var fullSizeImage: Boolean by kpref(
        "full_size_image",
        oldPrefs.fullSizeImage /* false */
    )

    override var autoExpandTextBox: Boolean by kpref("auto_expand_text_box", true)
}
