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
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BaseBundle
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.FrostWebActivity
import com.pitchedapps.frost.db.CookieEntity
import com.pitchedapps.frost.db.FrostDatabase
import com.pitchedapps.frost.db.latestEpoch
import com.pitchedapps.frost.db.saveNotifications
import com.pitchedapps.frost.enums.OverlayContext
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.parsers.FrostParser
import com.pitchedapps.frost.facebook.parsers.MessageParser
import com.pitchedapps.frost.facebook.parsers.NotifParser
import com.pitchedapps.frost.facebook.parsers.ParseNotification
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.settings.hasNotifications
import com.pitchedapps.frost.utils.ARG_USER_ID
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostEvent
import com.pitchedapps.frost.utils.isIndependent
import java.util.Locale
import kotlin.math.abs

/**
 * Created by Allan Wang on 2017-07-08.
 *
 * Logic for build notifications, scheduling notifications, and showing notifications
 */
private val _40_DP = 40.dpToPx

/**
 * Enum to handle notification creations
 */
enum class NotificationType(
    val channelId: String,
    private val overlayContext: OverlayContext,
    private val fbItem: FbItem,
    private val parser: FrostParser<ParseNotification>,
    private val ringtone: () -> String
) {

    GENERAL(
        NOTIF_CHANNEL_GENERAL,
        OverlayContext.NOTIFICATION,
        FbItem.NOTIFICATIONS,
        NotifParser,
        Prefs::notificationRingtone
    ),

    MESSAGE(
        NOTIF_CHANNEL_MESSAGES,
        OverlayContext.MESSAGE,
        FbItem.MESSAGES,
        MessageParser,
        Prefs::messageRingtone
    );

    private val groupPrefix = "frost_${name.toLowerCase(Locale.CANADA)}"

    /**
     * Optional binder to return the request bundle builder
     */
    internal open fun bindRequest(
        content: NotificationContent,
        cookie: String
    ): (BaseBundle.() -> Unit)? = null

    private fun bindRequest(intent: Intent, content: NotificationContent) {
        val cookie = content.data.cookie ?: return
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
    suspend fun fetch(context: Context, data: CookieEntity): Int {
        val notifDao = FrostDatabase.get().notifDao()
        val response = try {
            parser.parse(data.cookie)
        } catch (ignored: Exception) {
            null
        }
        if (response == null) {
            L.v { "$name notification data not found" }
            return -1
        }

        /**
         * Checks that the text doesn't contain any blacklisted keywords
         */
        fun validText(text: String?): Boolean {
            val t = text ?: return true
            return Prefs.notificationKeywords.none {
                t.contains(it, true)
            }
        }

        val notifContents = response.data.getUnreadNotifications(data).filter { notif ->
            validText(notif.title) && validText(notif.text)
        }
        if (notifContents.isEmpty()) return 0

        val userId = data.id
        val prevLatestEpoch = notifDao.latestEpoch(userId, channelId)
        L.v { "Notif $name prev epoch $prevLatestEpoch" }

        if (!notifDao.saveNotifications(channelId, notifContents)) {
            L.d { "Skip notifs for $name as saving failed" }
            return -1
        }

        if (prevLatestEpoch == -1L && !BuildConfig.DEBUG) {
            L.d { "Skipping first notification fetch" }
            return 0 // do not notify the first time
        }

        val newNotifContents = notifContents.filter { it.timestamp > prevLatestEpoch }

        if (newNotifContents.isEmpty()) {
            L.d { "No new notifs found for $name" }
            return 0
        }

        L.d { "${newNotifContents.size} new notifs found for $name" }

        val notifs = newNotifContents.map { createNotification(context, it) }

        frostEvent("Notifications", "Type" to name, "Count" to notifs.size)
        if (notifs.size > 1)
            summaryNotification(context, userId, notifs.size).notify(context)
        val ringtone = ringtone()
        notifs.forEachIndexed { i, notif ->
            // Ring at most twice
            notif.withAlert(context, i < 2, ringtone).notify(context)
        }
        return notifs.size
    }

    fun debugNotification(context: Context, data: CookieEntity) {
        val content = NotificationContent(
            data,
            System.currentTimeMillis(),
            "https://github.com/AllanWang/Frost-for-Facebook",
            "Debug Notif",
            "Test 123",
            System.currentTimeMillis() / 1000,
            "https://www.iconexperience.com/_img/v_collection_png/256x256/shadow/dog.png",
            false
        )
        createNotification(context, content).notify(context)
    }

    /**
     * Attach content related data to an intent
     */
    fun putContentExtra(intent: Intent, content: NotificationContent): Intent {
        // We will show the notification page for dependent urls. We can trigger a click next time
        intent.data =
            Uri.parse(if (content.href.isIndependent) content.href else FbItem.NOTIFICATIONS.url)
        bindRequest(intent, content)
        return intent
    }

    /**
     * Create a generic content for the provided type and user id.
     * No content related data is added
     */
    fun createCommonIntent(context: Context, userId: Long): Intent {
        val intent = Intent(context, FrostWebActivity::class.java)
        intent.putExtra(ARG_USER_ID, userId)
        overlayContext.put(intent)
        return intent
    }

    /**
     * Create and submit a new notification with the given [content]
     */
    private fun createNotification(
        context: Context,
        content: NotificationContent
    ): FrostNotification =
        with(content) {
            val intent = createCommonIntent(context, content.data.id)
            putContentExtra(intent, content)
            val group = "${groupPrefix}_${data.id}"
            val pendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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

            FrostNotification(group, notifId, notifBuilder)
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
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notifBuilder = context.frostNotification(channelId)
            .setContentTitle(context.string(R.string.frost_name))
            .setContentText("$count ${context.string(fbItem.titleId)}")
            .setGroup(group)
            .setGroupSummary(true)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_SOCIAL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifBuilder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
        }

        return FrostNotification(group, 1, notifBuilder)
    }
}

/**
 * Notification data holder
 */
data class NotificationContent(
    // TODO replace data with userId?
    val data: CookieEntity,
    val id: Long,
    val href: String,
    val title: String? = null, // defaults to frost title
    val text: String,
    val timestamp: Long,
    val profileUrl: String?,
    val unread: Boolean
) {

    val notifId = abs(id.toInt())
}

/**
 * Wrapper for a complete notification builder and identifier
 * which can be immediately notified when given a [Context]
 */
data class FrostNotification(
    private val tag: String,
    private val id: Int,
    val notif: NotificationCompat.Builder
) {

    fun withAlert(context: Context, enable: Boolean, ringtone: String): FrostNotification {
        notif.setFrostAlert(context, enable, ringtone)
        return this
    }

    fun notify(context: Context) =
        NotificationManagerCompat.from(context).notify(tag, id, notif.build())
}

fun Context.scheduleNotificationsFromPrefs(): Boolean {
    val shouldSchedule = Prefs.hasNotifications
    return if (shouldSchedule) scheduleNotifications(Prefs.notificationFreq)
    else scheduleNotifications(-1)
}

fun Context.scheduleNotifications(minutes: Long): Boolean =
    scheduleJob<NotificationService>(NOTIFICATION_PERIODIC_JOB, minutes)

fun Context.fetchNotifications(): Boolean =
    fetchJob<NotificationService>(NOTIFICATION_JOB_NOW)
