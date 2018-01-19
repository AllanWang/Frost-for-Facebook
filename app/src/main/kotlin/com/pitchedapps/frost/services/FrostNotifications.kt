package com.pitchedapps.frost.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.BaseBundle
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.color
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.string
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.FrostWebActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.NotificationModel
import com.pitchedapps.frost.dbflow.lastNotificationTime
import com.pitchedapps.frost.enums.OverlayContext
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.parsers.FrostParser
import com.pitchedapps.frost.parsers.MessageParser
import com.pitchedapps.frost.parsers.NotifParser
import com.pitchedapps.frost.parsers.ParseNotification
import com.pitchedapps.frost.utils.ARG_USER_ID
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostAnswersCustom
import org.jetbrains.anko.runOnUiThread
import java.util.*

/**
 * Created by Allan Wang on 2017-07-08.
 *
 * Logic for build notifications, scheduling notifications, and showing notifications
 */
fun setupNotificationChannels(c: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val mainChannel = NotificationChannel(BuildConfig.APPLICATION_ID, c.getString(R.string.frost_name), NotificationManager.IMPORTANCE_DEFAULT)
    mainChannel.lightColor = c.color(R.color.facebook_blue)
    mainChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    manager.createNotificationChannel(mainChannel)
}

inline val Context.frostNotification: NotificationCompat.Builder
    get() = NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID).apply {
        setSmallIcon(R.drawable.frost_f_24)
        setAutoCancel(true)
        setStyle(NotificationCompat.BigTextStyle())
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

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        builder.setLargeIcon(resource)
        NotificationManagerCompat.from(context).notify(notifTag, notifId, builder.build())
    }
}

/**
 * Enum to handle notification creations
 */
enum class NotificationType(
        private val overlayContext: OverlayContext,
        private val fbItem: FbItem,
        private val parser: FrostParser<ParseNotification>,
        private val getTime: (notif: NotificationModel) -> Long,
        private val putTime: (notif: NotificationModel, time: Long) -> NotificationModel,
        private val ringtone: () -> String) {

    GENERAL(OverlayContext.NOTIFICATION,
            FbItem.NOTIFICATIONS,
            NotifParser,
            NotificationModel::epoch,
            { notif, time -> notif.copy(epoch = time) },
            Prefs::notificationRingtone) {

        override fun bindRequest(content: NotificationContent, cookie: String) =
                FrostRunnable.prepareMarkNotificationRead(content.id, cookie)
    },

    MESSAGE(OverlayContext.MESSAGE,
            FbItem.MESSAGES,
            MessageParser,
            NotificationModel::epochIm,
            { notif, time -> notif.copy(epochIm = time) },
            Prefs::messageRingtone);

    private val groupPrefix = "frost_${name.toLowerCase(Locale.CANADA)}"

    /**
     * Optional binder to return the request bundle builder
     */
    internal open fun bindRequest(content: NotificationContent, cookie: String): (BaseBundle.() -> Unit)? = null

    private fun bindRequest(intent: Intent, content: NotificationContent, cookie: String?) {
        cookie ?: return
        val binder = bindRequest(content, cookie) ?: return
        val bundle = Bundle()
        bundle.binder()
        intent.putExtras(bundle)
    }

    /**
     * Get unread data from designated parser
     * Display notifications for those after old epoch
     * Save new epoch
     */
    fun fetch(context: Context, data: CookieModel) {
        val response = parser.parse(data.cookie)
                ?: return L.v { "$name notification data not found" }
        val notifs = response.data.getUnreadNotifications(data).filter {
            val text = it.text
            Prefs.notificationKeywords.none { text.contains(it, true) }
        }
        if (notifs.isEmpty()) return
        var notifCount = 0
        val userId = data.id
        val prevNotifTime = lastNotificationTime(userId)
        val prevLatestEpoch = getTime(prevNotifTime)
        L.v { "Notif $name prev epoch $prevLatestEpoch" }
        var newLatestEpoch = prevLatestEpoch
        notifs.forEach { notif ->
            L.v { "Notif timestamp ${notif.timestamp}" }
            if (notif.timestamp <= prevLatestEpoch) return@forEach
            createNotification(context, notif, notifCount == 0)
            if (notif.timestamp > newLatestEpoch)
                newLatestEpoch = notif.timestamp
            notifCount++
        }
        if (newLatestEpoch > prevLatestEpoch)
            putTime(prevNotifTime, newLatestEpoch).save()
        L.d { "Notif $name new epoch ${getTime(lastNotificationTime(userId))}" }
        summaryNotification(context, userId, notifCount)
    }

    /**
     * Create and submit a new notification with the given [content]
     * If [withDefaults] is set, it will also add the appropriate sound, vibration, and light
     * Note that when we have multiple notifications coming in at once, we don't want to have defaults for all of them
     */
    private fun createNotification(context: Context, content: NotificationContent, withDefaults: Boolean) {
        with(content) {
            val intent = Intent(context, FrostWebActivity::class.java)
            intent.data = Uri.parse(href)
            intent.putExtra(ARG_USER_ID, data.id)
            overlayContext.put(intent)
            bindRequest(intent, content, data.cookie)

            val group = "${groupPrefix}_${data.id}"
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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
            L.v { "Notif load $content" }
            NotificationManagerCompat.from(context).notify(group, notifId, notifBuilder.build())

            if (profileUrl != null) {
                context.runOnUiThread {
                    //todo verify if context is valid?
                    GlideApp.with(context)
                            .asBitmap()
                            .load(profileUrl)
                            .transform(FrostGlide.circleCrop)
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
    private fun summaryNotification(context: Context, userId: Long, count: Int) {
        frostAnswersCustom("Notifications", "Type" to name, "Count" to count)
        if (count <= 1) return
        val intent = Intent(context, FrostWebActivity::class.java)
        intent.data = Uri.parse(fbItem.url)
        intent.putExtra(ARG_USER_ID, userId)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notifBuilder = context.frostNotification.withDefaults(ringtone())
                .setContentTitle(context.string(R.string.frost_name))
                .setContentText("$count ${context.string(fbItem.titleId)}")
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
                               val id: Long,
                               val href: String,
                               val title: String? = null, // defaults to frost title
                               val text: String,
                               val timestamp: Long,
                               val profileUrl: String?) {

    val notifId = Math.abs(id.toInt())

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