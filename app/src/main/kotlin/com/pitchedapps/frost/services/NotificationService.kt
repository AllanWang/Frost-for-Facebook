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
import com.pitchedapps.frost.FrostWebActivity
import com.pitchedapps.frost.R
import com.pitchedapps.frost.WebOverlayActivity
import com.pitchedapps.frost.dbflow.*
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostAnswersCustom
import com.pitchedapps.frost.utils.frostNotification
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.Future

/**
 * Created by Allan Wang on 2017-06-14.
 */
class NotificationService : JobService() {

    var future: Future<Unit>? = null

    override fun onStopJob(params: JobParameters?): Boolean {
        future?.cancel(true)
        future = null
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        future = doAsync {
            //            debugNotification("Load notifs")
            loadFbCookiesSync().forEach {
                data ->
                L.i("Handle notifications for $data")
                val doc = Jsoup.connect(FbTab.NOTIFICATIONS.url).cookie(FACEBOOK_COM, data.cookie).get()
                val unreadNotifications = doc.getElementById("notifications_list").getElementsByClass("aclb")
                var notifCount = 0
                var latestEpoch = lastNotificationTime(data.id)
                L.v("Latest Epoch $latestEpoch")
                unreadNotifications.forEach unread@ {
                    elem ->
                    val notif = parseNotification(data, elem)
                    if (notif != null) {
                        if (notif.timestamp <= latestEpoch) return@unread
                        notif.createNotification(this@NotificationService)
                        latestEpoch = notif.timestamp
                        notifCount++
                    }
                }
                if (notifCount > 0) saveNotificationTime(NotificationModel(data.id, latestEpoch))
                frostAnswersCustom("Notifications") { putCustomAttribute("Count", notifCount) }
                summaryNotification(data.id, notifCount)
            }
            L.d("Finished notifications")
            jobFinished(params, false)
            future = null
        }
        return true
    }

    companion object {
        val epochMatcher: Regex by lazy { Regex(":([0-9]*),") }
        val notifIdMatcher: Regex by lazy { Regex("notif_id\":([0-9]*),") }
    }

    fun parseNotification(data: CookieModel, element: Element): NotificationContent? {
        val a = element.getElementsByTag("a").first() ?: return null
        val dataStore = a.attr("data-store")
        val notifId = if (dataStore == null) System.currentTimeMillis()
        else notifIdMatcher.find(dataStore)?.groups?.get(1)?.value?.toLong() ?: System.currentTimeMillis()
        val abbr = element.getElementsByTag("abbr")
        val timeString = abbr?.text()
        var text = a.text().replace("\u00a0", " ") //remove &nbsp;
        if (timeString != null) text = text.removeSuffix(timeString)
        text = text.trim()
        val abbrData = abbr?.attr("data-store")
        val epoch = if (abbrData == null) -1L else epochMatcher.find(abbrData)?.groups?.get(1)?.value?.toLong() ?: -1L
        return NotificationContent(data, notifId.toInt(), a.attr("href"), text, epoch)
    }

    private fun Context.debugNotification(text: String) {
        if (BuildConfig.DEBUG) {
            val notifBuilder = frostNotification
                    .setContentTitle(string(R.string.app_name))
                    .setContentText(text)

            NotificationManagerCompat.from(this).notify(999, notifBuilder.build())
        }
    }

    data class NotificationContent(val data: CookieModel, val notifId: Int, val href: String, val text: String, val timestamp: Long) {
        fun createNotification(context: Context, verifiedUser: Boolean = false) {
            //in case we haven't found the name, we will try one more time before passing the notification
            if (!verifiedUser && data.name?.isBlank() ?: true) {
                data.fetchUsername {
                    data.name = it
                    createNotification(context, true)
                }
            } else {
                val intent = Intent(context, FrostWebActivity::class.java)
                intent.data = Uri.parse("$FB_URL_BASE$href")
                intent.putExtra(WebOverlayActivity.ARG_USER_ID, data.id)
                val group = "frost_${data.id}"
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                val notifBuilder = context.frostNotification
                        .setContentTitle(context.string(R.string.app_name))
                        .setContentText(text)
                        .setContentIntent(pendingIntent)
                        .setCategory(Notification.CATEGORY_SOCIAL)
                        .setSubText(data.name)
                        .setGroup(group)

                if (timestamp != -1L) notifBuilder.setWhen(timestamp * 1000)

                NotificationManagerCompat.from(context).notify(group, notifId, notifBuilder.build())
            }
        }
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