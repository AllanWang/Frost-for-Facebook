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
import com.pitchedapps.frost.enums.MainActivityLayout
import com.pitchedapps.frost.prefs.OldPrefs
import com.pitchedapps.frost.prefs.PrefsBase
import org.koin.core.KoinComponent
import org.koin.core.inject

interface FeedPrefs : PrefsBase {
    var webTextScaling: Int

    var feedSort: Int

    var aggressiveRecents: Boolean

    var showComposer: Boolean

    var showSuggestedFriends: Boolean

    var showSuggestedGroups: Boolean

    var showFacebookAds: Boolean

    var showStories: Boolean

    var mainActivityLayoutType: Int

    val mainActivityLayout: MainActivityLayout

    var showPostActions: Boolean

    var showPostReactions: Boolean
}

class FeedPrefsImpl(
    factory: KPrefFactory
) : KPref("${BuildConfig.APPLICATION_ID}.prefs.feed", factory),
    FeedPrefs, KoinComponent {

    private val oldPrefs: OldPrefs by inject()

    override var webTextScaling: Int by kpref("web_text_scaling", oldPrefs.webTextScaling /* 100 */)

    override var feedSort: Int by kpref(
        "feed_sort",
        oldPrefs.feedSort /* FeedSort.DEFAULT.ordinal */
    )

    override var aggressiveRecents: Boolean by kpref(
        "aggressive_recents",
        oldPrefs.aggressiveRecents /* false */
    )

    override var showComposer: Boolean by kpref(
        "status_composer_feed",
        oldPrefs.showComposer /* true */
    )

    override var showSuggestedFriends: Boolean by kpref(
        "suggested_friends_feed",
        oldPrefs.showSuggestedFriends /* true */
    )

    override var showSuggestedGroups: Boolean by kpref(
        "suggested_groups_feed",
        oldPrefs.showSuggestedGroups /* true */
    )

    override var showFacebookAds: Boolean by kpref(
        "facebook_ads",
        oldPrefs.showFacebookAds /* false */
    )

    override var showStories: Boolean by kpref("show_stories", oldPrefs.showStories /* true */)

    override var mainActivityLayoutType: Int by kpref(
        "main_activity_layout_type",
        oldPrefs.mainActivityLayoutType /* 0 */
    )

    override val mainActivityLayout: MainActivityLayout
        get() = MainActivityLayout(mainActivityLayoutType)

    override var showPostActions: Boolean by kpref("show_post_actions", true)

    override var showPostReactions: Boolean by kpref("show_post_reactions", true)
}
