package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.utils.minuteToText
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
fun SettingsActivity.getNotificationPrefs(): KPrefAdapterBuilder.() -> Unit = {

    text(R.string.notification_frequency, { Prefs.notificationFreq }, { Prefs.notificationFreq = it }) {
        val options = longArrayOf(-1, 15, 30, 60, 120, 180, 300, 1440, 2880)
        val texts = options.map { minuteToText(it) }
        onClick = {
            _, _, item ->
            materialDialogThemed {
                title(R.string.notification_frequency)
                items(texts)
                itemsCallbackSingleChoice(options.indexOf(item.pref), {
                    _, _, which, _ ->
                    item.pref = options[which]
                    scheduleNotifications(item.pref)
                    true
                })
            }
            true
        }
        textGetter = { minuteToText(it) }
    }

    plainText(R.string.notification_keywords) {
        descRes = R.string.notification_keywords_desc
        onClick = {
            _, _, _ ->
            val keywordView = Keywords(this@getNotificationPrefs)
            materialDialogThemed {
                title(R.string.notification_keywords)
                customView(keywordView, false)
                dismissListener { keywordView.save() }
                positiveText(R.string.kau_done)
            }
            true
        }
    }

    checkbox(R.string.notification_all_accounts, { Prefs.notificationAllAccounts }, { Prefs.notificationAllAccounts = it }) {
        descRes = R.string.notification_all_accounts_desc
    }

    checkbox(R.string.notification_messages, { Prefs.notificationsInstantMessages }, { Prefs.notificationsInstantMessages = it }) {
        descRes = R.string.notification_messages_desc
    }

    checkbox(R.string.notification_sound, { Prefs.notificationSound }, { Prefs.notificationSound = it })

    checkbox(R.string.notification_vibrate, { Prefs.notificationVibrate }, { Prefs.notificationVibrate = it })

    checkbox(R.string.notification_lights, { Prefs.notificationLights }, { Prefs.notificationLights = it })

    plainText(R.string.notification_fetch_now) {
        descRes = R.string.notification_fetch_now_desc
        onClick = {
            _, _, _ ->
            val text = if (fetchNotifications()) R.string.notification_fetch_success else R.string.notification_fetch_fail
            frostSnackbar(text)
            true
        }
    }


}