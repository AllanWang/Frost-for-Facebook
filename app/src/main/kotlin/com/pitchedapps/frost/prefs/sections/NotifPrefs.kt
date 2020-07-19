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

interface NotifPrefs : PrefsBase {
    var notificationKeywords: Set<String>

    var notificationsGeneral: Boolean

    var notificationAllAccounts: Boolean

    var notificationsInstantMessages: Boolean

    var notificationsImAllAccounts: Boolean

    var notificationVibrate: Boolean

    var notificationSound: Boolean

    var notificationRingtone: String

    var messageRingtone: String

    var notificationLights: Boolean

    var notificationFreq: Long
}

class NotifPrefsImpl(
    factory: KPrefFactory
) : KPref("${BuildConfig.APPLICATION_ID}.prefs.notif", factory),
    NotifPrefs, KoinComponent {

    private val oldPrefs: OldPrefs by inject()

    override var notificationKeywords: Set<String> by kpref(
        "notification_keywords",
        oldPrefs.notificationKeywords /* mutableSetOf() */
    )

    override var notificationsGeneral: Boolean by kpref(
        "notification_general",
        oldPrefs.notificationsGeneral /* true */
    )

    override var notificationAllAccounts: Boolean by kpref(
        "notification_all_accounts",
        oldPrefs.notificationAllAccounts /* true */
    )

    override var notificationsInstantMessages: Boolean by kpref(
        "notification_im",
        oldPrefs.notificationsInstantMessages /* true */
    )

    override var notificationsImAllAccounts: Boolean by kpref(
        "notification_im_all_accounts",
        oldPrefs.notificationsImAllAccounts /* false */
    )

    override var notificationVibrate: Boolean by kpref(
        "notification_vibrate",
        oldPrefs.notificationVibrate /* true */
    )

    override var notificationSound: Boolean by kpref(
        "notification_sound",
        oldPrefs.notificationSound /* true */
    )

    override var notificationRingtone: String by kpref(
        "notification_ringtone",
        oldPrefs.notificationRingtone /* "" */
    )

    override var messageRingtone: String by kpref(
        "message_ringtone",
        oldPrefs.messageRingtone /* "" */
    )

    override var notificationLights: Boolean by kpref(
        "notification_lights",
        oldPrefs.notificationLights /* true */
    )

    override var notificationFreq: Long by kpref(
        "notification_freq",
        oldPrefs.notificationFreq /* 15L */
    )
}
