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
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.ARG_USER_ID
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.withRoundIcon
import org.jetbrains.anko.runOnUiThread

/**
 * Created by Allan Wang on 2017-07-08.
 *
 * Logic for build notifications, scheduling notifications, and showing notifications
 */

/**
 * Wrap the default builder with our icon and accent color
 */
val Context.frostNotification: NotificationCompat.Builder
    get() = NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID).apply {
        setSmallIcon(R.drawable.frost_f_24)
        setAutoCancel(true)
        color = color(R.color.frost_notification_accent)
    }

/**
 * Assign global changes to the notification after it is built
 */
@Suppress("DEPRECATION")
        //The update feature is for Android O and seems to still be in beta
fun Notification.frostConfig() = apply {
    if (Prefs.notificationVibrate) defaults = defaults or Notification.DEFAULT_VIBRATE
    if (Prefs.notificationSound) defaults = defaults or Notification.DEFAULT_SOUND
    if (Prefs.notificationLights) defaults = defaults or Notification.DEFAULT_LIGHTS
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
        NotificationManagerCompat.from(context).notify(notifTag, notifId, builder.withBigText.build().frostConfig())
    }
}

internal const val FROST_NOTIFICATION_GROUP = "frost"
internal const val FROST_MESSAGE_NOTIFICATION_GROUP = "frost_im"

/**
 * Notification data holder
 */
data class NotificationContent(val data: CookieModel,
                               val notifId: Int,
                               val href: String,
                               val title: String? = null,
                               val text: String,
                               val timestamp: Long,
                               val profileUrl: String) {
    fun createNotification(context: Context) = createNotification(context, FROST_NOTIFICATION_GROUP)

    fun createMessageNotification(context: Context) = createNotification(context, FROST_MESSAGE_NOTIFICATION_GROUP)

    private fun createNotification(context: Context, groupPrefix: String) {
        val intent = Intent(context, FrostWebActivity::class.java)
        intent.data = Uri.parse(href.formattedFbUrl)
        intent.putExtra(ARG_USER_ID, data.id)
        val group = "${groupPrefix}_${data.id}"
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val notifBuilder = context.frostNotification
                .setContentTitle(title ?: context.string(R.string.frost_name))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SOCIAL)
                .setSubText(data.name)
                .setGroup(group)

        if (timestamp != -1L) notifBuilder.setWhen(timestamp * 1000)
        L.v("Notif load", this.toString())
        NotificationManagerCompat.from(context).notify(group, notifId, notifBuilder.withBigText.build().frostConfig())

        if (profileUrl.isNotBlank()) {
            context.runOnUiThread {
                Glide.with(context)
                        .asBitmap()
                        .load(profileUrl)
                        .withRoundIcon()
                        .into(FrostNotificationTarget(context, notifId, group, notifBuilder))
            }
        }
    }
}

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
    val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
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