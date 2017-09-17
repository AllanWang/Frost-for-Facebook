package com.pitchedapps.frost.services

import android.app.Notification
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.color
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.string
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.FrostWebActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.enums.OverlayContext
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.*
import org.jetbrains.anko.runOnUiThread

/**
 * Created by Allan Wang on 2017-07-08.
 *
 * Logic for build notifications, scheduling notifications, and showing notifications
 */


val Context.frostNotification: NotificationCompat.Builder
    get() = NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID).apply {
        setSmallIcon(R.drawable.frost_f_24)
        setAutoCancel(true)
        color = color(R.color.frost_notification_accent)
    }

fun NotificationCompat.Builder.withDefaults(ringtone: String = Prefs.notificationRingtone) = apply {
    var defaults = 0
    if (Prefs.notificationVibrate) defaults = defaults or Notification.DEFAULT_VIBRATE
    if (Prefs.notificationSound) {
        if (ringtone.isNotBlank()) setSound(Uri.parse(ringtone))
        else defaults = defaults or Notification.DEFAULT_SOUND
    }
    if (Prefs.notificationLights) defaults = defaults or Notification.DEFAULT_LIGHTS
    setDefaults(defaults)
}

val NotificationCompat.Builder.withBigText: NotificationCompat.BigTextStyle
    get() = NotificationCompat.BigTextStyle(this)

/**
 * Created by Allan Wang on 2017-07-08.
 *
 * Custom target to set the content view and update a given notification
 * 40dp is the size of the right avatar
 */
class FrostNotificationTarget(val context: Context,
                              val notifId: Int,
                              val notifTag: String,
                              val builder: NotificationCompat.Builder
) : SimpleTarget<Bitmap>(40.dpToPx, 40.dpToPx) {

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
        builder.setLargeIcon(resource)
        NotificationManagerCompat.from(context).notify(notifTag, notifId, builder.withBigText.build())
    }
}

/**
 * Enum to handle notification creations
 */
enum class NotificationType(
        private val groupPrefix: String,
        private val overlayContext: OverlayContext,
        private val contentRes: Int,
        private val pendingUrl: String,
        private val ringtone: () -> String) {
    GENERAL("frost", OverlayContext.NOTIFICATION, R.string.notifications, FbItem.NOTIFICATIONS.url, { Prefs.notificationRingtone }),
    MESSAGE("frost_im", OverlayContext.MESSAGE, R.string.messages, FbItem.MESSAGES.url, { Prefs.messageRingtone });

    /**
     * Create and submit a new notification with the given [content]
     * If [withDefaults] is set, it will also add the appropriate sound, vibration, and light
     * Note that when we have multiple notifications coming in at once, we don't want to have defaults for all of them
     */
    fun createNotification(context: Context, content: NotificationContent, withDefaults: Boolean) {
        with(content) {
            val intent = Intent(context, FrostWebActivity::class.java)
            intent.data = Uri.parse(href.formattedFbUrl)
            intent.putExtra(ARG_USER_ID, data.id)
            intent.putExtra(ARG_OVERLAY_CONTEXT, overlayContext)
            val group = "${groupPrefix}_${data.id}"
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            val notifBuilder = context.frostNotification
                    .setContentTitle(title ?: context.string(R.string.frost_name))
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setCategory(Notification.CATEGORY_SOCIAL)
                    .setSubText(data.name)
                    .setGroup(group)

            if (withDefaults)
                notifBuilder.withDefaults(ringtone())

            if (timestamp != -1L) notifBuilder.setWhen(timestamp * 1000)
            L.v("Notif load", context.toString())
            NotificationManagerCompat.from(context).notify(group, notifId, notifBuilder.withBigText.build())

            if (profileUrl.isNotBlank()) {
                context.runOnUiThread {
                    //todo verify if context is valid?
                    Glide.with(context)
                            .asBitmap()
                            .load(profileUrl)
                            .withRoundIcon()
                            .into(FrostNotificationTarget(context, notifId, group, notifBuilder))
                }
            }
        }
    }

    /**
     * Create a summary notification to wrap the previous ones
     * This will always produce sound, vibration, and lights based on preferences
     * and will only show if we have at least 2 notifications
     */
    fun summaryNotification(context: Context, userId: Long, count: Int) {
        frostAnswersCustom("Notifications", "Type" to name, "Count" to count)
        if (count <= 1) return
        val intent = Intent(context, FrostWebActivity::class.java)
        intent.data = Uri.parse(pendingUrl)
        intent.putExtra(ARG_USER_ID, userId)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val notifBuilder = context.frostNotification.withDefaults(ringtone())
                .setContentTitle(context.string(R.string.frost_name))
                .setContentText("$count ${context.string(contentRes)}")
                .setGroup("${groupPrefix}_$userId")
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SOCIAL)

        NotificationManagerCompat.from(context).notify("${groupPrefix}_$userId", userId.toInt(), notifBuilder.build())
    }
}

/**
 * Notification data holder
 */
data class NotificationContent(val data: CookieModel,
                               val notifId: Int,
                               val href: String,
                               val title: String? = null,
                               val text: String,
                               val timestamp: Long,
                               val profileUrl: String)

const val NOTIFICATION_PERIODIC_JOB = 7

/**
 * [interval] is # of min, which must be at least 15
 * returns false if an error occurs; true otherwise
 */
fun Context.scheduleNotifications(minutes: Long): Boolean {
    val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    scheduler.cancel(NOTIFICATION_PERIODIC_JOB)
    if (minutes < 0L) return true
    val serviceComponent = ComponentName(this, NotificationService::class.java)
    val builder = JobInfo.Builder(NOTIFICATION_PERIODIC_JOB, serviceComponent)
            .setPeriodic(minutes * 60000)
            .setPersisted(true)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) //TODO add options
    val result = scheduler.schedule(builder.build())
    if (result <= 0) {
        L.eThrow("Notification scheduler failed")
        return false
    }
    return true
}

const val NOTIFICATION_JOB_NOW = 6

/**
 * Run notification job right now
 */
fun Context.fetchNotifications(): Boolean {
    val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler? ?: return false
    val serviceComponent = ComponentName(this, NotificationService::class.java)
    val builder = JobInfo.Builder(NOTIFICATION_JOB_NOW, serviceComponent)
            .setMinimumLatency(0L)
            .setOverrideDeadline(2000L)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
    val result = scheduler.schedule(builder.build())
    if (result <= 0) {
        L.eThrow("Notification scheduler failed")
        return false
    }
    return true
}