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
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.parsers.MessageParser
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostAnswersCustom
import com.pitchedapps.frost.utils.frostJsoup
import org.jetbrains.anko.doAsync
import org.jsoup.nodes.Element
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

    companion object {
        val epochMatcher: Regex by lazy { Regex(":([0-9]*?),") }
        val notifIdMatcher: Regex by lazy { Regex("notif_id\":([0-9]*?),") }
        val messageNotifIdMatcher: Regex by lazy { Regex("thread_fbid_([0-9]+)") }
        val profMatcher: Regex by lazy { Regex("url\\(\"(.*?)\"\\)") }
    }

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
                if (Prefs.notificationsInstantMessages && (current || Prefs.notificationsImAllAccounts))
                    fetchMessageNotifications(it)
            }
            finish(params)
        }
        return true
    }

    fun logNotif(text: String): NotificationContent? {
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

    fun fetchGeneralNotifications(data: CookieModel) {
        L.d("Notif fetch", data.toString())
        val doc = frostJsoup(data.cookie, FbItem.NOTIFICATIONS.url)
        //aclb for unread, acw for read
        val unreadNotifications = (doc.getElementById("notifications_list") ?: return L.eThrow("Notification list not found")).getElementsByClass("aclb")
        var notifCount = 0
        //val prevLatestEpoch = 1498931565L // for testing
        val prevNotifTime = lastNotificationTime(data.id)
        val prevLatestEpoch = prevNotifTime.epoch
        L.v("Notif Prev Latest Epoch $prevLatestEpoch")
        var newLatestEpoch = prevLatestEpoch
        unreadNotifications.forEach unread@ { elem ->
            val notif = parseNotification(data, elem) ?: return@unread
            L.v("Notif timestamp ${notif.timestamp}")
            if (notif.timestamp <= prevLatestEpoch) return@unread
            NotificationType.GENERAL.createNotification(this, notif, notifCount == 0)
            if (notif.timestamp > newLatestEpoch)
                newLatestEpoch = notif.timestamp
            notifCount++
        }
        if (newLatestEpoch != prevLatestEpoch) prevNotifTime.copy(epoch = newLatestEpoch).save()
        L.d("Notif new latest epoch ${lastNotificationTime(data.id).epoch}")
        NotificationType.GENERAL.summaryNotification(this, data.id, notifCount)
    }

    fun parseNotification(data: CookieModel, element: Element): NotificationContent? {
        val a = element.getElementsByTag("a").first() ?: return logNotif("IM No a tag")
        val abbr = element.getElementsByTag("abbr")
        val epoch = epochMatcher.find(abbr.attr("data-store"))?.groups?.get(1)?.value?.toLong() ?: return logNotif("IM No epoch")
        //fetch id
        val notifId = notifIdMatcher.find(a.attr("data-store"))?.groups?.get(1)?.value?.toLong() ?: System.currentTimeMillis()
        val timeString = abbr.text()
        val text = a.text().replace("\u00a0", " ").removeSuffix(timeString).trim() //remove &nbsp;
        if (Prefs.notificationKeywords.any { text.contains(it, ignoreCase = true) }) return null //notification filtered out
        //fetch profpic
        val p = element.select("i.img[style*=url]")
        val pUrl = profMatcher.find(p.attr("style"))?.groups?.get(1)?.value?.formattedFbUrl ?: ""
        return NotificationContent(data, notifId.toInt(), a.attr("href"), null, text, epoch, pUrl)
    }

    /*
     * ----------------------------------------------------------------
     * Instant message notification logic.
     * Fetch notifications -> Filter new ones -> Parse notifications ->
     * Show notifications -> Show group notification
     * ----------------------------------------------------------------
     */

    fun fetchMessageNotifications(data: CookieModel) {
        L.d("Notif IM fetch", data.toString())
        val doc = frostJsoup(data.cookie, FbItem.MESSAGES.url)
        val (threads, _, _) = MessageParser.parse(doc.toString()) ?: return L.e("Could not parse IM")

        var notifCount = 0
        val prevNotifTime = lastNotificationTime(data.id)
        val prevLatestEpoch = prevNotifTime.epochIm
        L.v("Notif Prev Latest Im Epoch $prevLatestEpoch")
        var newLatestEpoch = prevLatestEpoch
        threads.filter { it.unread }.forEach { notif ->
            L.v("Notif Im timestamp ${notif.time}")
            if (notif.time <= prevLatestEpoch) return@forEach
            NotificationType.MESSAGE.createNotification(this, NotificationContent(data, notif), notifCount == 0)
            if (notif.time > newLatestEpoch)
                newLatestEpoch = notif.time
            notifCount++
        }
        if (newLatestEpoch != prevLatestEpoch) prevNotifTime.copy(epochIm = newLatestEpoch).save()
        L.d("Notif new latest im epoch ${lastNotificationTime(data.id).epochIm}")
        NotificationType.MESSAGE.summaryNotification(this, data.id, notifCount)
    }

    private fun Context.debugNotification(text: String) {
        if (!BuildConfig.DEBUG) return
        val notifBuilder = frostNotification.withDefaults()
                .setContentTitle(string(R.string.frost_name))
                .setContentText(text)
        NotificationManagerCompat.from(this).notify(999, notifBuilder.build())
    }

}