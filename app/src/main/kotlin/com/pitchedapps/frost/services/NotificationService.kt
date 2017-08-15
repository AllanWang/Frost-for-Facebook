package com.pitchedapps.frost.services

import android.app.Notification
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.FrostWebActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.dbflow.lastNotificationTime
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.loadFbCookiesSync
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.utils.ARG_USER_ID
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostAnswersCustom
import com.pitchedapps.frost.web.launchHeadlessHtmlExtractor
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
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
            if (Prefs.notificationAllAccounts) {
                val cookies = loadFbCookiesSync()
                cookies.forEach { fetchGeneralNotifications(it) }
            } else {
                val currentCookie = loadFbCookie(Prefs.userId)
                if (currentCookie != null) {
                    fetchGeneralNotifications(currentCookie)
                }
            }
            L.d("Finished main notifications")
            if (Prefs.notificationsInstantMessages) {
                val currentCookie = loadFbCookie(Prefs.userId)
                if (currentCookie != null) {
                    fetchMessageNotifications(currentCookie) {
                        L.i("Notif IM fetching finished ${if (it) "succesfully" else "unsuccessfully"}")
                        finish(params)
                    }
                    return@doAsync
                }
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
        val doc = Jsoup.connect(FbItem.NOTIFICATIONS.url).cookie(FACEBOOK_COM, data.cookie).userAgent(USER_AGENT_BASIC).get()
        //aclb for unread, acw for read
        val unreadNotifications = (doc.getElementById("notifications_list") ?: return L.eThrow("Notification list not found")).getElementsByClass("aclb")
        var notifCount = 0
        //val prevLatestEpoch = 1498931565L // for testing
        val prevNotifTime = lastNotificationTime(data.id)
        val prevLatestEpoch = prevNotifTime.epoch
        L.v("Notif Prev Latest Epoch $prevLatestEpoch")
        var newLatestEpoch = prevLatestEpoch
        unreadNotifications.forEach unread@ {
            elem ->
            val notif = parseNotification(data, elem) ?: return@unread
            L.v("Notif timestamp ${notif.timestamp}")
            if (notif.timestamp <= prevLatestEpoch) return@unread
            notif.createNotification(this@NotificationService)
            if (notif.timestamp > newLatestEpoch)
                newLatestEpoch = notif.timestamp
            notifCount++
        }
        if (newLatestEpoch != prevLatestEpoch) prevNotifTime.copy(epoch = newLatestEpoch).save()
        L.d("Notif new latest epoch ${lastNotificationTime(data.id).epoch}")
        frostAnswersCustom("Notifications", "Type" to "General", "Count" to notifCount)
        summaryNotification(data.id, notifCount)
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

    fun summaryNotification(userId: Long, count: Int)
            = summaryNotification(userId, count, R.string.notifications, FbItem.NOTIFICATIONS.url, FROST_NOTIFICATION_GROUP)

    /*
     * ----------------------------------------------------------------
     * Instant message notification logic.
     * Fetch notifications -> Filter new ones -> Parse notifications ->
     * Show notifications -> Show group notification
     * ----------------------------------------------------------------
     */

    inline fun fetchMessageNotifications(data: CookieModel, crossinline callback: (success: Boolean) -> Unit) {
        launchHeadlessHtmlExtractor(FbItem.MESSAGES.url, JsAssets.NOTIF_MSG) {
            it.observeOn(Schedulers.newThread()).subscribe {
                (html, errorRes) ->
                L.d("Notf IM html received")
                if (errorRes != -1) return@subscribe callback(false)
                fetchMessageNotifications(data, html)
                callback(true)
            }
        }
    }

    fun fetchMessageNotifications(data: CookieModel, html: String) {
        L.d("Notif IM fetch", data.toString())
        val doc = Jsoup.parseBodyFragment(html)
        val unreadNotifications = (doc.getElementById("threadlist_rows") ?: return L.eThrow("Notification messages not found")).getElementsByClass("aclb")
        var notifCount = 0
        val prevNotifTime = lastNotificationTime(data.id)
        val prevLatestEpoch = prevNotifTime.epochIm
        L.v("Notif Prev Latest Im Epoch $prevLatestEpoch")
        var newLatestEpoch = prevLatestEpoch
        unreadNotifications.forEach unread@ {
            elem ->
            val notif = parseMessageNotification(data, elem) ?: return@unread
            L.v("Notif im timestamp ${notif.timestamp}")
            if (notif.timestamp <= prevLatestEpoch) return@unread
            notif.createMessageNotification(this@NotificationService)
            if (notif.timestamp > newLatestEpoch)
                newLatestEpoch = notif.timestamp
            notifCount++
        }
        if (newLatestEpoch != prevLatestEpoch) prevNotifTime.copy(epochIm = newLatestEpoch).save()
        L.d("Notif new latest im epoch ${lastNotificationTime(data.id).epochIm}")
        frostAnswersCustom("Notifications", "Type" to "Message", "Count" to notifCount)
        summaryMessageNotification(data.id, notifCount)
    }

    fun parseMessageNotification(data: CookieModel, element: Element): NotificationContent? {
        val a = element.getElementsByTag("a").first() ?: return null
        val abbr = element.getElementsByTag("abbr")
        val epoch = epochMatcher.find(abbr.attr("data-store"))?.groups?.get(1)?.value?.toLong() ?: return logNotif("No epoch")
        val thread = element.getElementsByAttributeValueContaining("id", "thread_fbid_").first() ?: return null
        //fetch id
        val notifId = messageNotifIdMatcher.find(thread.id())?.groups?.get(1)?.value?.toLong() ?: System.currentTimeMillis()
        val text = element.select("span.snippet").firstOrNull()?.text()?.trim() ?: getString(R.string.new_message)
        if (Prefs.notificationKeywords.any { text.contains(it, ignoreCase = true) }) return null //notification filtered out
        //fetch convo pic
        val p = element.select("i.img[style*=url]")
        val pUrl = profMatcher.find(p.attr("style"))?.groups?.get(1)?.value?.formattedFbUrl ?: ""
        L.v("url", a.attr("href"))
        return NotificationContent(data, notifId.toInt(), a.attr("href"), a.text(), text, epoch, pUrl)
    }

    fun summaryMessageNotification(userId: Long, count: Int)
            = summaryNotification(userId, count, R.string.messages, FbItem.MESSAGES.url, FROST_MESSAGE_NOTIFICATION_GROUP)

    private fun Context.debugNotification(text: String) {
        if (!BuildConfig.DEBUG) return
        val notifBuilder = frostNotification
                .setContentTitle(string(R.string.frost_name))
                .setContentText(text)
        NotificationManagerCompat.from(this).notify(999, notifBuilder.build().frostConfig())
    }

    private fun summaryNotification(userId: Long, count: Int, contentRes: Int, pendingUrl: String, groupPrefix: String) {
        if (count <= 1) return
        val intent = Intent(this, FrostWebActivity::class.java)
        intent.data = Uri.parse(pendingUrl)
        intent.putExtra(ARG_USER_ID, userId)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notifBuilder = frostNotification
                .setContentTitle(string(R.string.frost_name))
                .setContentText("$count ${string(contentRes)}")
                .setGroup("${groupPrefix}_$userId")
                .setGroupSummary(true)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SOCIAL)

        NotificationManagerCompat.from(this).notify("${groupPrefix}_$userId", userId.toInt(), notifBuilder.build().frostConfig())
    }

}