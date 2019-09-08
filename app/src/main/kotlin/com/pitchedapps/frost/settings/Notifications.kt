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

import android.annotation.SuppressLint
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.kpref.activity.items.KPrefText
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.minuteToText
import ca.allanwang.kau.utils.string
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.db.FrostDatabase
import com.pitchedapps.frost.db.deleteAll
import com.pitchedapps.frost.services.fetchNotifications
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.REQUEST_NOTIFICATION
import com.pitchedapps.frost.utils.frostSnackbar
import com.pitchedapps.frost.utils.frostUri
import com.pitchedapps.frost.views.Keywords
import kotlinx.coroutines.launch

/**
 * Created by Allan Wang on 2017-06-29.
 */

val Prefs.hasNotifications: Boolean
    get() = notificationsGeneral || notificationsInstantMessages

@SuppressLint("InlinedApi")
fun SettingsActivity.getNotificationPrefs(): KPrefAdapterBuilder.() -> Unit = {

    text(
        R.string.notification_frequency,
        Prefs::notificationFreq,
        { Prefs.notificationFreq = it }) {
        val options = longArrayOf(15, 30, 60, 120, 180, 300, 1440, 2880)
        val texts =
            options.map { if (it <= 0) string(R.string.no_notifications) else minuteToText(it) }
        onClick = {
            materialDialog {
                title(R.string.notification_frequency)
                listItemsSingleChoice(
                    items = texts,
                    initialSelection = options.indexOf(item.pref)
                ) { _, index, _ ->
                    item.pref = options[index]
                    setFrostResult(REQUEST_NOTIFICATION)
                }
            }
        }
        enabler = { Prefs.hasNotifications }
        textGetter = { minuteToText(it) }
    }

    plainText(R.string.notification_keywords) {
        descRes = R.string.notification_keywords_desc
        onClick = {
            val keywordView = Keywords(this@getNotificationPrefs)
            materialDialog {
                title(R.string.notification_keywords)
                customView(view = keywordView)
                positiveButton(R.string.kau_done)
                onDismiss { keywordView.save() }
            }
        }
    }

    checkbox(R.string.notification_general, Prefs::notificationsGeneral,
        {
            Prefs.notificationsGeneral = it
            reloadByTitle(R.string.notification_general_all_accounts)
            if (!Prefs.notificationsInstantMessages)
                reloadByTitle(R.string.notification_frequency)
        }) {
        descRes = R.string.notification_general_desc
    }

    checkbox(R.string.notification_general_all_accounts, Prefs::notificationAllAccounts,
        { Prefs.notificationAllAccounts = it }) {
        descRes = R.string.notification_general_all_accounts_desc
        enabler = { Prefs.notificationsGeneral }
    }

    checkbox(R.string.notification_messages, Prefs::notificationsInstantMessages,
        {
            Prefs.notificationsInstantMessages = it
            reloadByTitle(R.string.notification_messages_all_accounts)
            if (!Prefs.notificationsGeneral)
                reloadByTitle(R.string.notification_frequency)
        }) {
        descRes = R.string.notification_messages_desc
    }

    checkbox(R.string.notification_messages_all_accounts, Prefs::notificationsImAllAccounts,
        { Prefs.notificationsImAllAccounts = it }) {
        descRes = R.string.notification_messages_all_accounts_desc
        enabler = { Prefs.notificationsInstantMessages }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        plainText(R.string.notification_channel) {
            descRes = R.string.notification_channel_desc
            onClick = {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent)
            }
        }
    } else {
        checkbox(R.string.notification_sound, Prefs::notificationSound, {
            Prefs.notificationSound = it
            reloadByTitle(
                R.string.notification_ringtone,
                R.string.message_ringtone
            )
        })

        fun KPrefText.KPrefTextContract<String>.ringtone(code: Int) {
            enabler = Prefs::notificationSound
            textGetter = {
                if (it.isBlank()) string(R.string.kau_default)
                else RingtoneManager.getRingtone(this@getNotificationPrefs, frostUri(it))
                    ?.getTitle(this@getNotificationPrefs)
                    ?: "---" //todo figure out why this happens
            }
            onClick = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, string(R.string.select_ringtone))
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                    if (item.pref.isNotBlank()) {
                        putExtra(
                            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                            frostUri(item.pref)
                        )
                    }
                }
                startActivityForResult(intent, code)
            }
        }

        text(R.string.notification_ringtone, Prefs::notificationRingtone,
            { Prefs.notificationRingtone = it }) {
            ringtone(SettingsActivity.REQUEST_NOTIFICATION_RINGTONE)
        }

        text(R.string.message_ringtone, Prefs::messageRingtone,
            { Prefs.messageRingtone = it }) {
            ringtone(SettingsActivity.REQUEST_MESSAGE_RINGTONE)
        }

        checkbox(R.string.notification_vibrate, Prefs::notificationVibrate,
            { Prefs.notificationVibrate = it })

        checkbox(R.string.notification_lights, Prefs::notificationLights,
            { Prefs.notificationLights = it })
    }

    if (BuildConfig.DEBUG) {
        plainText(R.string.reset_notif_epoch) {
            onClick = {
                launch {
                    FrostDatabase.get().notifDao().deleteAll()
                }
            }
        }
    }

    plainText(R.string.notification_fetch_now) {
        descRes = R.string.notification_fetch_now_desc
        onClick = {
            val text =
                if (fetchNotifications()) R.string.notification_fetch_success
                else R.string.notification_fetch_fail
            frostSnackbar(text)
        }
    }
}
