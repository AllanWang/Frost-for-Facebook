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
package com.pitchedapps.frost.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ca.allanwang.kau.utils.color
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostUri

/**
 * Created by Allan Wang on 07/04/18.
 */
const val NOTIF_CHANNEL_GENERAL = "general"
const val NOTIF_CHANNEL_MESSAGES = "messages"

fun setupNotificationChannels(c: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val appName = c.string(R.string.frost_name)
    val msg = c.string(R.string.messages)
    manager.createNotificationChannel(NOTIF_CHANNEL_GENERAL, appName)
    manager.createNotificationChannel(NOTIF_CHANNEL_MESSAGES, "$appName: $msg")
    manager.notificationChannels
        .filter {
            it.id != NOTIF_CHANNEL_GENERAL &&
                it.id != NOTIF_CHANNEL_MESSAGES
        }
        .forEach { manager.deleteNotificationChannel(it.id) }
    L.d { "Created notification channels: ${manager.notificationChannels.size} channels, ${manager.notificationChannelGroups.size} groups" }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun NotificationManager.createNotificationChannel(
    id: String,
    name: String
): NotificationChannel {
    val channel = NotificationChannel(
        id,
        name, NotificationManager.IMPORTANCE_DEFAULT
    )
    channel.enableLights(true)
    channel.lightColor = Prefs.accentColor
    channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    createNotificationChannel(channel)
    return channel
}

fun Context.frostNotification(id: String) =
    NotificationCompat.Builder(this, id)
        .apply {
            setSmallIcon(R.drawable.frost_f_24)
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            setStyle(NotificationCompat.BigTextStyle())
            color = color(R.color.frost_notification_accent)
        }

/**
 * Dictates whether a notification should have sound/vibration/lights or not
 * Delegates to channels if Android O and up
 * Otherwise uses our provided preferences
 */
fun NotificationCompat.Builder.setFrostAlert(
    context: Context,
    enable: Boolean,
    ringtone: String
): NotificationCompat.Builder {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        setGroupAlertBehavior(
            if (enable) NotificationCompat.GROUP_ALERT_CHILDREN
            else NotificationCompat.GROUP_ALERT_SUMMARY
        )
    } else if (!enable) {
        setDefaults(0)
    } else {
        var defaults = 0
        if (Prefs.notificationVibrate) defaults = defaults or Notification.DEFAULT_VIBRATE
        if (Prefs.notificationSound) {
            if (ringtone.isNotBlank()) setSound(context.frostUri(ringtone))
            else defaults = defaults or Notification.DEFAULT_SOUND
        }
        if (Prefs.notificationLights) defaults = defaults or Notification.DEFAULT_LIGHTS
        setDefaults(defaults)
    }
    return this
}

/*
 * -----------------------------------
 * Job Scheduler
 * -----------------------------------
 */

const val NOTIFICATION_PARAM_ID = "notif_param_id"

fun JobInfo.Builder.setExtras(id: Int): JobInfo.Builder {
    val bundle = PersistableBundle()
    bundle.putInt(NOTIFICATION_PARAM_ID, id)
    return setExtras(bundle)
}

/**
 * interval is # of min, which must be at least 15
 * returns false if an error occurs; true otherwise
 */
inline fun <reified T : JobService> Context.scheduleJob(id: Int, minutes: Long): Boolean {
    val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    scheduler.cancel(id)
    if (minutes < 0L) return true
    val serviceComponent = ComponentName(this, T::class.java)
    val builder = JobInfo.Builder(id, serviceComponent)
        .setPeriodic(minutes * 60000)
        .setExtras(id)
        .setPersisted(true)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) //TODO add options
    val result = scheduler.schedule(builder.build())
    if (result <= 0) {
        L.eThrow("${T::class.java.simpleName} scheduler failed")
        return false
    }
    return true
}

/**
 * Run notification job right now
 */
inline fun <reified T : JobService> Context.fetchJob(id: Int): Boolean {
    val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    val serviceComponent = ComponentName(this, T::class.java)
    val builder = JobInfo.Builder(id, serviceComponent)
        .setMinimumLatency(0L)
        .setExtras(id)
        .setOverrideDeadline(2000L)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
    val result = scheduler.schedule(builder.build())
    if (result <= 0) {
        L.eThrow("${T::class.java.simpleName} instant scheduler failed")
        return false
    }
    return true
}
