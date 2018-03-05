package com.pitchedapps.frost.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.kpref.activity.items.KPrefText
import ca.allanwang.kau.utils.minuteToText
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.services.fetchNotifications
import com.pitchedapps.frost.services.scheduleNotifications
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostSnackbar
import com.pitchedapps.frost.utils.materialDialogThemed
import com.pitchedapps.frost.views.Keywords


/**
 * Created by Allan Wang on 2017-06-29.
 */
@SuppressLint("InlinedApi")
fun SettingsActivity.getNotificationPrefs(): KPrefAdapterBuilder.() -> Unit = {

    text(R.string.notification_frequency, Prefs::notificationFreq, { Prefs.notificationFreq = it }) {
        val options = longArrayOf(15, 30, 60, 120, 180, 300, 1440, 2880)
        val texts = options.map { if (it <= 0) string(R.string.no_notifications) else minuteToText(it) }
        onClick = {
            materialDialogThemed {
                title(R.string.notification_frequency)
                items(texts)
                itemsCallbackSingleChoice(options.indexOf(item.pref), { _, _, which, _ ->
                    item.pref = options[which]
                    scheduleNotifications(item.pref)
                    true
                })
            }
        }
        enabler = {
            val enabled = Prefs.notificationsGeneral || Prefs.notificationsInstantMessages
            if (!enabled)
                scheduleNotifications(-1)
            enabled
        }
        textGetter = { minuteToText(it) }
    }

    plainText(R.string.notification_keywords) {
        descRes = R.string.notification_keywords_desc
        onClick = {
            val keywordView = Keywords(this@getNotificationPrefs)
            materialDialogThemed {
                title(R.string.notification_keywords)
                customView(keywordView, false)
                dismissListener { keywordView.save() }
                positiveText(R.string.kau_done)
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
        enabler = Prefs::notificationsGeneral
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
        enabler = Prefs::notificationsInstantMessages
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
            reloadByTitle(R.string.notification_ringtone,
                    R.string.message_ringtone)
        })

        fun KPrefText.KPrefTextContract<String>.ringtone(code: Int) {
            enabler = Prefs::notificationSound
            textGetter = {
                if (it.isBlank()) string(R.string.kau_default)
                else RingtoneManager.getRingtone(this@getNotificationPrefs, Uri.parse(it))
                        ?.getTitle(this@getNotificationPrefs)
                        ?: "---" //todo figure out why this happens
            }
            onClick = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, string(R.string.select_ringtone))
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                    if (item.pref.isNotBlank())
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(item.pref))
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