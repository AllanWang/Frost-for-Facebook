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
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.loadFbCookiesSync
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostAnswersCustom
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.Future

/**
 * Created by Allan Wang on 2017-06-14.
 *
 * Service to manage notifications
 * Will periodically check through all accounts in the db and send notifications when appropriate
 */
class NotificationService : JobService() {

    var future: Future<Unit>? = null

    companion object {
        val epochMatcher: Regex by lazy { Regex(":([0-9]*?),") }
        val notifIdMatcher: Regex by lazy { Regex("notif_id\":([0-9]*?),") }
        val messageNotifIdMatcher: Regex by lazy { Regex("thread_fbid_([0-9]+)") }
        val profMatcher: Regex by lazy { Regex("url\\(\"(.*?)\"\\)") }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        future?.cancel(true)
        future = null
        return false
    }


    override fun onStartJob(params: JobParameters?): Boolean {
        future = doAsync {
            if (Prefs.notificationAllAccounts) {
                val cookies = loadFbCookiesSync()
                cookies.forEach { fetchGeneralNotifications(it) }
//                if (Prefs.notificationsInstantMessages) {
//                    Prefs.prevId = Prefs.userId
//                    uiThread {
//                        val messageWebView = MessageWebView(this@NotificationService, params)
//                        cookies.forEach { messageWebView.request(it) }
//                    }
//                    return@doAsync
//                }
            } else {
                val currentCookie = loadFbCookie(Prefs.userId)
                if (currentCookie != null) {
                    fetchGeneralNotifications(currentCookie)
//                    if (Prefs.notificationsInstantMessages) {
//                        uiThread { MessageWebView(this@NotificationService, params).request(currentCookie) }
//                        return@doAsync
//                    }
                }
            }
            L.d("Finished notifications")
            jobFinished(params, false)
            future = null
        }
        return true
    }

    fun logNotif(text: String): NotificationContent? {
        L.eThrow("NotificationService: $text")
        return null
    }

    fun fetchGeneralNotifications(data: CookieModel) {
        L.i("Notif fetch for $data")
        val doc = Jsoup.connect(FbTab.NOTIFICATIONS.url).cookie(FACEBOOK_COM, data.cookie).userAgent(USER_AGENT_BASIC).get()
        //aclb for unread, acw for read
        val unreadNotifications = (doc.getElementById("notifications_list") ?: return L.eThrow("Notification list not found")).getElementsByClass("aclb")
        var notifCount = 0
//        val prevLatestEpoch = 1498931565L // for testing
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
        frostAnswersCustom("Notifications") {
            putCustomAttribute("Type", "General")
            putCustomAttribute("Count", notifCount)
        }
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
        val pUrl = profMatcher.find(p.attr("style"))?.groups?.get(1)?.value ?: ""
        return NotificationContent(data, notifId.toInt(), a.attr("href"), null, text, epoch, pUrl)
    }

    fun fetchMessageNotifications(data: CookieModel, content: String) {
        L.i("Notif IM fetch for $data")
        val doc = Jsoup.parseBodyFragment(content)
        val unreadNotifications = (doc.getElementById("threadlist_rows") ?: return L.eThrow("Notification messages not found")).getElementsByClass("aclb")
        var notifCount = 0
        L.d("IM notif count ${unreadNotifications.size}")
        unreadNotifications.forEach {
            with(it) {
                L.d("notif ${id()} ${className()}")
            }
        }
        val prevNotifTime = lastNotificationTime(data.id)
        val prevLatestEpoch = prevNotifTime.epochIm
        L.v("Notif Prev Latest Im Epoch $prevLatestEpoch")
        var newLatestEpoch = prevLatestEpoch
        unreadNotifications.forEach unread@ {
            elem ->
            val notif = parseMessageNotification(data, elem) ?: return@unread
            L.v("Notif im timestamp ${notif.timestamp}")
            if (notif.timestamp <= prevLatestEpoch) return@unread
            notif.createNotification(this@NotificationService)
            if (notif.timestamp > newLatestEpoch)
                newLatestEpoch = notif.timestamp
            notifCount++
        }
        if (newLatestEpoch != prevLatestEpoch) prevNotifTime.copy(epochIm = newLatestEpoch).save()
        L.d("Notif new latest im epoch ${lastNotificationTime(data.id).epochIm}")
        frostAnswersCustom("Notifications") {
            putCustomAttribute("Type", "Message")
            putCustomAttribute("Count", notifCount)
        }
        summaryNotification(data.id, notifCount)
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
        val pUrl = profMatcher.find(p.attr("style"))?.groups?.get(1)?.value ?: ""
        return NotificationContent(data, notifId.toInt(), a.attr("href"), a.text(), text, epoch, pUrl)
    }

    private fun Context.debugNotification(text: String) {
        if (!BuildConfig.DEBUG) return
        val notifBuilder = frostNotification
                .setContentTitle(string(R.string.app_name))
                .setContentText(text)
        NotificationManagerCompat.from(this).notify(999, notifBuilder.build().frostConfig())
    }

    fun summaryNotification(userId: Long, count: Int) {
        if (count <= 1) return
        val notifBuilder = frostNotification
                .setContentTitle(string(R.string.app_name))
                .setContentText("$count notifications")
                .setGroup("frost_$userId")
                .setGroupSummary(true)

        NotificationManagerCompat.from(this).notify("frost_$userId", userId.toInt(), notifBuilder.build().frostConfig())
    }

}