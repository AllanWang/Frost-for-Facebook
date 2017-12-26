package com.pitchedapps.frost.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.lastNotificationTime
import com.pitchedapps.frost.dbflow.loadFbCookiesSync
import com.pitchedapps.frost.parsers.FrostNotif
import com.pitchedapps.frost.parsers.FrostThread
import com.pitchedapps.frost.parsers.MessageParser
import com.pitchedapps.frost.parsers.NotifParser
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostAnswersCustom
import org.jetbrains.anko.doAsync
import java.util.concurrent.Future

/**
 * Created by Allan Wang on 2017-06-14.
 *
 * Service to manage notifications
 * Will periodically check through all accounts in the db and send notifications when appropriate
 *
 * Note that general notifications are parsed directly with Jsoup,
 * but instant messages are done so with a headless webview as it is generated from JS
 */
class NotificationService : JobService() {

    var future: Future<Unit>? = null

    val startTime = System.currentTimeMillis()

    override fun onStopJob(params: JobParameters?): Boolean {
        val time = System.currentTimeMillis() - startTime
        L.d("Notification service has finished abruptly in $time ms")
        frostAnswersCustom("NotificationTime",
                "Type" to "Service force stop",
                "IM Included" to Prefs.notificationsInstantMessages,
                "Duration" to time)
        future?.cancel(true)
        future = null
        return false
    }

    fun finish(params: JobParameters?) {
        val time = System.currentTimeMillis() - startTime
        L.i("Notification service has finished in $time ms")
        frostAnswersCustom("NotificationTime",
                "Type" to "Service",
                "IM Included" to Prefs.notificationsInstantMessages,
                "Duration" to time)
        jobFinished(params, false)
        future?.cancel(true)
        future = null
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        L.i("Fetching notifications")
        future = doAsync {
            val currentId = Prefs.userId
            val cookies = loadFbCookiesSync()
            cookies.forEach {
                val current = it.id == currentId
                if (current || Prefs.notificationAllAccounts)
                    fetchGeneralNotifications(it)
                if (Prefs.notificationsInstantMessages
                        && (current || Prefs.notificationsImAllAccounts))
                    fetchMessageNotifications(it)
            }
            finish(params)
        }
        return true
    }

    private fun logNotif(text: String): NotificationContent? {
        L.eThrow("NotificationService: $text")
        return null
    }

    /*
     * ----------------------------------------------------------------
     * General notification logic.
     * Fetch notifications -> Filter new ones -> Parse notifications ->
     * Show notifications -> Show group notification
     * ----------------------------------------------------------------
     */

    private fun fetchGeneralNotifications(data: CookieModel) {
        val unreadNotifications = NotifParser.parse(data)?.data?.notifs
                ?.filter(FrostNotif::unread)?.map { it.toNotification(data) }
                ?: return L.eThrow("Notification data not found")
        createNotifications(NotificationType.GENERAL, unreadNotifications)
    }

    /*
     * ----------------------------------------------------------------
     * Instant message notification logic.
     * Fetch notifications -> Filter new ones -> Parse notifications ->
     * Show notifications -> Show group notification
     * ----------------------------------------------------------------
     */

    private fun fetchMessageNotifications(data: CookieModel) {
        val unreadNotifications = MessageParser.parse(data)?.data?.threads
                ?.filter(FrostThread::unread)?.map { it.toNotification(data) }
                ?: return L.eThrow("Message notification data not found")
        createNotifications(NotificationType.MESSAGE, unreadNotifications)
    }

    /**
     * Generate notification data from the given [type] and [notifs]
     * Will also keep track of the epoch times and update accordingly
     */
    private fun createNotifications(type: NotificationType, notifs: List<NotificationContent>) {
        if (notifs.isEmpty()) return
        var notifCount = 0
        val userId = notifs[1].data.id
        val prevNotifTime = lastNotificationTime(userId)
        val prevLatestEpoch = type.timeLong(prevNotifTime)
        L.v("Notif ${type.name} prev epoch $prevLatestEpoch")
        var newLatestEpoch = prevLatestEpoch
        notifs.forEach { notif ->
            L.v("Notif timestamp ${notif.timestamp}")
            if (notif.timestamp <= prevLatestEpoch) return@forEach
            type.createNotification(this, notif, notifCount == 0)
            if (notif.timestamp > newLatestEpoch)
                newLatestEpoch = notif.timestamp
            notifCount++
        }
        if (newLatestEpoch != prevLatestEpoch)
            type.saveTime(prevNotifTime, newLatestEpoch).save()
        L.d("Notif ${type.name} new epoch ${type.timeLong(lastNotificationTime(userId))}")
        type.summaryNotification(this, userId, notifCount)
    }

    private fun Context.debugNotification(text: String) {
        if (!BuildConfig.DEBUG) return
        val notifBuilder = frostNotification.withDefaults()
                .setContentTitle(string(R.string.frost_name))
                .setContentText(text)
        NotificationManagerCompat.from(this).notify(999, notifBuilder.build())
    }

}