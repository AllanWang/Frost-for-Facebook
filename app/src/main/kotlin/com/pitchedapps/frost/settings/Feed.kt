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
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.string
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.enums.FeedSort
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.REQUEST_FAB

/**
 * Created by Allan Wang on 2017-06-29.
 */
fun SettingsActivity.getFeedPrefs(): KPrefAdapterBuilder.() -> Unit = {

    text(R.string.newsfeed_sort, Prefs::feedSort, { Prefs.feedSort = it }) {
        descRes = R.string.newsfeed_sort_desc
        onClick = {
            materialDialog {
                title(R.string.newsfeed_sort)
                listItemsSingleChoice(
                    items = FeedSort.values().map { string(it.textRes) },
                    initialSelection = item.pref
                ) { _, index, _ ->
                    if (item.pref != index) {
                        item.pref = index
                        shouldRestartMain()
                    }
                }
            }
        }
        textGetter = { string(FeedSort(it).textRes) }
    }

    checkbox(R.string.aggressive_recents, Prefs::aggressiveRecents, {
        Prefs.aggressiveRecents = it
        shouldRefreshMain()
    }) {
        descRes = R.string.aggressive_recents_desc
    }

    checkbox(R.string.composer, Prefs::showComposer, {
        Prefs.showComposer = it
        shouldRefreshMain()
    }) {
        descRes = R.string.composer_desc
    }

    checkbox(R.string.create_fab, Prefs::showCreateFab, {
        Prefs.showCreateFab = it
        setFrostResult(REQUEST_FAB)
    }) {
        descRes = R.string.create_fab_desc
    }

    checkbox(R.string.suggested_friends, Prefs::showSuggestedFriends, {
        Prefs.showSuggestedFriends = it
        shouldRefreshMain()
    }) {
        descRes = R.string.suggested_friends_desc
    }

    checkbox(R.string.suggested_groups, Prefs::showSuggestedGroups, {
        Prefs.showSuggestedGroups = it
        shouldRefreshMain()
    }) {
        descRes = R.string.suggested_groups_desc
    }

    checkbox(R.string.show_stories, Prefs::showStories, {
        Prefs.showStories = it
        shouldRefreshMain()
    }) {
        descRes = R.string.show_stories_desc
    }

    checkbox(R.string.facebook_ads, Prefs::showFacebookAds, {
        Prefs.showFacebookAds = it
        shouldRefreshMain()
    }) {
        descRes = R.string.facebook_ads_desc
    }
}
