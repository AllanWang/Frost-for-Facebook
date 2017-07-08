package com.pitchedapps.frost.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.*
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FbTab
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
                loadFbCookiesSync().forEach {
                    data ->
                    fetchNotifications(data)
                }
            } else {
                val currentCookie = loadFbCookie(Prefs.userId)
                if (currentCookie != null)
                    fetchNotifications(currentCookie)
            }
            L.d("Finished notifications")
            jobFinished(params, false)
            future = null
        }
        return true
    }

    fun fetchNotifications(data: CookieModel) {
        L.i("Notif fetch for $data")
        val doc = Jsoup.connect(FbTab.NOTIFICATIONS.url).cookie(FACEBOOK_COM, data.cookie).get()
        //aclb for unread, acw for read
        val unreadNotifications = doc.getElementById("notifications_list").getElementsByClass("aclb")
        var notifCount = 0
//        val prevLatestEpoch = 1498931565L // for testing
        val prevLatestEpoch = lastNotificationTime(data.id)
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
        if (newLatestEpoch != prevLatestEpoch) saveNotificationTime(NotificationModel(data.id, newLatestEpoch))
        frostAnswersCustom("Notifications") { putCustomAttribute("Count", notifCount) }
        summaryNotification(data.id, notifCount)
    }


    fun parseNotification(data: CookieModel, element: Element): NotificationContent? {
        val a = element.getElementsByTag("a").first() ?: return null
        //fetch id
        val dataStore = a.attr("data-store")
        val notifId = if (dataStore == null) System.currentTimeMillis()
        else notifIdMatcher.find(dataStore)?.groups?.get(1)?.value?.toLong() ?: System.currentTimeMillis()
        val abbr = element.getElementsByTag("abbr")
        val timeString = abbr?.text()
        var text = a.text().replace("\u00a0", " ") //remove &nbsp;
        if (Prefs.notificationKeywords.any { text.contains(it, ignoreCase = true) }) return null //notification filtered out
        if (timeString != null) text = text.removeSuffix(timeString)
        text = text.trim()
        //fetch epoch
        val abbrData = abbr?.attr("data-store")
        val epoch = if (abbrData == null) -1L else epochMatcher.find(abbrData)?.groups?.get(1)?.value?.toLong() ?: -1L
        //fetch profpic
        val p = element.select("i.img[style*=url]")
        val pUrl = profMatcher.find(p.getOrNull(0)?.attr("style") ?: "")?.groups?.get(1)?.value ?: ""
        return NotificationContent(data, notifId.toInt(), a.attr("href"), text, epoch, pUrl)
    }

    private fun Context.debugNotification(text: String) {
        if (!BuildConfig.DEBUG) return
        val notifBuilder = frostNotification
                .setContentTitle(string(R.string.app_name))
                .setContentText(text)
        NotificationManagerCompat.from(this).notify(999, notifBuilder.build())
    }

    fun summaryNotification(userId: Long, count: Int) {
        if (count <= 1) return
        val notifBuilder = frostNotification
                .setContentTitle(string(R.string.app_name))
                .setContentText("$count notifications")
                .setGroup("frost_$userId")
                .setGroupSummary(true)

        NotificationManagerCompat.from(this).notify("frost_$userId", userId.toInt(), notifBuilder.build())
    }

}