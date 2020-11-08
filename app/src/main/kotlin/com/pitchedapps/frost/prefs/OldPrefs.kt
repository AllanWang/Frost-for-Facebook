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
package com.pitchedapps.frost.prefs

import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.KPrefFactory
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.enums.FeedSort

/**
 * Created by Allan Wang on 2017-05-28.
 *
 * Shared Preference object with lazy cached retrievals
 *
 * As of 2020-07-18, prefs have been split up into multiple folders
 */
@Deprecated(level = DeprecationLevel.WARNING, message = "Use pref segments")
class OldPrefs(factory: KPrefFactory) : KPref("${BuildConfig.APPLICATION_ID}.prefs", factory) {

    var lastLaunch: Long by kpref("last_launch", -1L)

    var userId: Long by kpref("user_id", -1L)

    var prevId: Long by kpref("prev_id", -1L)

    var theme: Int by kpref("theme", 0)

    var customTextColor: Int by kpref("color_text", 0xffeceff1.toInt())

    var customAccentColor: Int by kpref("color_accent", 0xff0288d1.toInt())

    var customBackgroundColor: Int by kpref("color_bg", 0xff212121.toInt())

    var customHeaderColor: Int by kpref("color_header", 0xff01579b.toInt())

    var customIconColor: Int by kpref("color_icons", 0xffeceff1.toInt())

    var exitConfirmation: Boolean by kpref("exit_confirmation", true)

    var notificationFreq: Long by kpref("notification_freq", 15L)

    var versionCode: Int by kpref("version_code", -1)

    var prevVersionCode: Int by kpref("prev_version_code", -1)

    var installDate: Long by kpref("install_date", -1L)

    var identifier: Int by kpref("identifier", -1)

    var tintNavBar: Boolean by kpref("tint_nav_bar", true)

    var webTextScaling: Int by kpref("web_text_scaling", 100)

    var feedSort: Int by kpref("feed_sort", FeedSort.DEFAULT.ordinal)

    var aggressiveRecents: Boolean by kpref("aggressive_recents", false)

    var showComposer: Boolean by kpref("status_composer_feed", true)

    var showSuggestedFriends: Boolean by kpref("suggested_friends_feed", true)

    var showSuggestedGroups: Boolean by kpref("suggested_groups_feed", true)

    var showFacebookAds: Boolean by kpref("facebook_ads", false)

    var showStories: Boolean by kpref("show_stories", true)

    var animate: Boolean by kpref("fancy_animations", true)

    var notificationKeywords: Set<String> by kpref("notification_keywords", mutableSetOf())

    var notificationsGeneral: Boolean by kpref("notification_general", true)

    var notificationAllAccounts: Boolean by kpref("notification_all_accounts", true)

    var notificationsInstantMessages: Boolean by kpref("notification_im", true)

    var notificationsImAllAccounts: Boolean by kpref("notification_im_all_accounts", false)

    var notificationVibrate: Boolean by kpref("notification_vibrate", true)

    var notificationSound: Boolean by kpref("notification_sound", true)

    var notificationRingtone: String by kpref("notification_ringtone", "")

    var messageRingtone: String by kpref("message_ringtone", "")

    var notificationLights: Boolean by kpref("notification_lights", true)

    var messageScrollToBottom: Boolean by kpref("message_scroll_to_bottom", false)

    var enablePip: Boolean by kpref("enable_pip", true)

    /**
     * Despite the naming, this toggle currently only enables debug logging.
     * Verbose is never logged in release builds.
     */
    var verboseLogging: Boolean by kpref("verbose_logging", false)

    var analytics: Boolean by kpref("analytics", false) {
//        if (!BuildConfig.DEBUG) {
//            if (it) {
//                Bugsnag.setAutoCaptureSessions(true)
//                Bugsnag.enableExceptionHandler()
//            } else {
//                Bugsnag.setAutoCaptureSessions(false)
//                Bugsnag.disableExceptionHandler()
//            }
//        }
    }

    var biometricsEnabled: Boolean by kpref("biometrics_enabled", false)

    var overlayEnabled: Boolean by kpref("overlay_enabled", true)

    var overlayFullScreenSwipe: Boolean by kpref("overlay_full_screen_swipe", true)

    var viewpagerSwipe: Boolean by kpref("viewpager_swipe", true)

    var loadMediaOnMeteredNetwork: Boolean by kpref("media_on_metered_network", true)

    var debugSettings: Boolean by kpref("debug_settings", false)

    var linksInDefaultApp: Boolean by kpref("link_in_default_app", false)

    var mainActivityLayoutType: Int by kpref("main_activity_layout_type", 0)

    var blackMediaBg: Boolean by kpref("black_media_bg", false)

    var autoRefreshFeed: Boolean by kpref("auto_refresh_feed", false)

    var showCreateFab: Boolean by kpref("show_create_fab", true)

    var fullSizeImage: Boolean by kpref("full_size_image", false)
}
