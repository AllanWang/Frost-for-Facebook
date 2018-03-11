package com.pitchedapps.frost.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BaseBundle
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.color
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.string
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
import java.util.*

/**
 * Created by Allan Wang on 2017-07-08.
 *
 * Logic for build notifications, scheduling notifications, and showing notifications
 */
const val NOTIF_CHANNEL_GENERAL = "general"
const val NOTIF_CHANNEL_MESSAGES = "messages"

fun setupNotificationChannels(c: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val appName = c.string(R.string.frost_name)
    val msg = c.string(R.string.messages)
    manager.notificationChannels
            .filter { it.id != NOTIF_CHANNEL_GENERAL && it.id != NOTIF_CHANNEL_MESSAGES }
            .forEach { manager.deleteNotificationChannel(it.id) }
    manager.createNotificationChannel(NOTIF_CHANNEL_GENERAL, appName)
    manager.createNotificationChannel(NOTIF_CHANNEL_MESSAGES, "$appName: $msg")
    L.d { "Created notification channels: ${manager.notificationChannels.size} channels, ${manager.notificationChannelGroups.size} groups" }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun NotificationManager.createNotificationChannel(id: String, name: String): NotificationChannel {
    val channel = NotificationChannel(id,
            name, NotificationManager.IMPORTANCE_DEFAULT)
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
fun NotificationCompat.Builder.setFrostAlert(enable: Boolean, ringtone: String): NotificationCompat.Builder {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        setGroupAlertBehavior(
                if (enable) Notification.GROUP_ALERT_CHILDREN
                else Notification.GROUP_ALERT_SUMMARY)
    } else if (!enable) {
       setDefaults(0)
    } else {
        var defaults = 0
        if (Prefs.notificationVibrate) defaults = defaults or Notification.DEFAULT_VIBRATE
        if (Prefs.notificationSound) {
            if (ringtone.isNotBlank()) setSound(Uri.parse(ringtone))
            else defaults = defaults or Notification.DEFAULT_SOUND
        }
        if (Prefs.notificationLights) defaults = defaults or Notification.DEFAULT_LIGHTS
        setDefaults(defaults)
    }
    return this
}

private val _40_DP = 40.dpToPx

/**
 * Enum to handle notification creations
 */
enum class NotificationType(
        private val channelId: String,
        private val overlayContext: OverlayContext,
        private val fbItem: FbItem,
        private val parser: FrostParser<ParseNotification>,
        private val getTime: (notif: NotificationModel) -> Long,
        private val putTime: (notif: NotificationModel, time: Long) -> NotificationModel,
        private val ringtone: () -> String) {

    GENERAL(NOTIF_CHANNEL_GENERAL,
            OverlayContext.NOTIFICATION,
            FbItem.NOTIFICATIONS,
            NotifParser,
            NotificationModel::epoch,
            { notif, time -> notif.copy(epoch = time) },
            Prefs::notificationRingtone) {

        override fun bindRequest(content: NotificationContent, cookie: String) =
                FrostRunnable.prepareMarkNotificationRead(content.id, cookie)
    },

    MESSAGE(NOTIF_CHANNEL_MESSAGES,
            OverlayContext.MESSAGE,
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
     *
     * Returns the number of notifications generated,
     * or -1 if an error occurred
     */
    fun fetch(context: Context, data: CookieModel): Int {
        val response = parser.parse(data.cookie)
        if (response == null) {
            L.v { "$name notification data not found" }
            return -1
        }
        val notifContents = response.data.getUnreadNotifications(data).filter {
            val text = it.text
            Prefs.notificationKeywords.none { text.contains(it, true) }
        }
        if (notifContents.isEmpty()) return 0
        val userId = data.id
        val prevNotifTime = lastNotificationTime(userId)
        val prevLatestEpoch = getTime(prevNotifTime)
        L.v { "Notif $name prev epoch $prevLatestEpoch" }
        var newLatestEpoch = prevLatestEpoch
        val notifs = mutableListOf<FrostNotification>()
        notifContents.forEach { notif ->
            L.v { "Notif timestamp ${notif.timestamp}" }
            if (notif.timestamp <= prevLatestEpoch) return@forEach
            notifs.add(createNotification(context, notif))
            if (notif.timestamp > newLatestEpoch)
                newLatestEpoch = notif.timestamp
        }
        if (newLatestEpoch > prevLatestEpoch)
            putTime(prevNotifTime, newLatestEpoch).save()
        L.d { "Notif $name new epoch ${getTime(lastNotificationTime(userId))}" }
        frostAnswersCustom("Notifications", "Type" to name, "Count" to notifs.size)
        if (notifs.size > 1)
            summaryNotification(context, userId, notifs.size).notify(context)
        val ringtone = ringtone()
        notifs.forEachIndexed { i, notif ->
            notif.withAlert(i < 2, ringtone).notify(context)
        }
        return notifs.size
    }

    fun debugNotification(context: Context, data: CookieModel) {
        val content = NotificationContent(data,
                System.currentTimeMillis(),
                "https://github.com/AllanWang/Frost-for-Facebook",
                "Debug Notif",
                "Test 123",
                System.currentTimeMillis() / 1000,
                "https://www.iconexperience.com/_img/v_collection_png/256x256/shadow/dog.png")
        createNotification(context, content).notify(context)
    }

    /**
     * Create and submit a new notification with the given [content]
     */
    private fun createNotification(context: Context, content: NotificationContent): FrostNotification {
        with(content) {
            val intent = Intent(context, FrostWebActivity::class.java)
            intent.data = Uri.parse(href)
            intent.putExtra(ARG_USER_ID, data.id)
            overlayContext.put(intent)
            bindRequest(intent, content, data.cookie)

            val group = "${groupPrefix}_${data.id}"
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val notifBuilder = context.frostNotification(channelId)
                    .setContentTitle(title ?: context.string(R.string.frost_name))
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setCategory(Notification.CATEGORY_SOCIAL)
                    .setSubText(data.name)
                    .setGroup(group)

            if (timestamp != -1L) notifBuilder.setWhen(timestamp * 1000)
            L.v { "Notif load $content" }

            if (profileUrl != null) {
                try {
                    val profileImg = GlideApp.with(context)
                            .asBitmap()
                            .load(profileUrl)
                            .transform(FrostGlide.circleCrop)
                            .submit(_40_DP, _40_DP)
                            .get()
                    notifBuilder.setLargeIcon(profileImg)
                } catch (e: Exception) {
                    L.e { "Failed to get image $profileUrl" }
                }
            }

            return FrostNotification(group, notifId, notifBuilder)
        }
    }


    /**
     * Create a summary notification to wrap the previous ones
     * This will always produce sound, vibration, and lights based on preferences
     * and will only show if we have at least 2 notifications
     */
    private fun summaryNotification(context: Context, userId: Long, count: Int): FrostNotification {
        val intent = Intent(context, FrostWebActivity::class.java)
        intent.data = Uri.parse(fbItem.url)
        intent.putExtra(ARG_USER_ID, userId)
        val group = "${groupPrefix}_$userId"
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notifBuilder = context.frostNotification(channelId)
                .setContentTitle(context.string(R.string.frost_name))
                .setContentText("$count ${context.string(fbItem.titleId)}")
                .setGroup(group)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SOCIAL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifBuilder.setGroupAlertBehavior(Notification.GROUP_ALERT_CHILDREN)
        }

        return FrostNotification(group, 1, notifBuilder)
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

/**
 * Wrapper for a complete notification builder and identifier
 * which can be immediately notified when given a [Context]
 */
data class FrostNotification(private val tag: String,
                             private val id: Int,
                             val notif: NotificationCompat.Builder) {

    fun withAlert(enable: Boolean, ringtone: String): FrostNotification {
        notif.setFrostAlert(enable, ringtone)
        return this
    }

    fun notify(context: Context) =
            NotificationManagerCompat.from(context).notify(tag, id, notif.build())
}

const val NOTIFICATION_PARAM_ID = "notif_param_id"

private fun JobInfo.Builder.setExtras(id: Int): JobInfo.Builder {
    val bundle = PersistableBundle()
    bundle.putInt(NOTIFICATION_PARAM_ID, id)
    return setExtras(bundle)
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
            .setExtras(NOTIFICATION_PERIODIC_JOB)
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
            .setExtras(NOTIFICATION_JOB_NOW)
            .setOverrideDeadline(2000L)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
    val result = scheduler.schedule(builder.build())
    if (result <= 0) {
        L.eThrow("Notification scheduler failed")
        return false
    }
    return true
}