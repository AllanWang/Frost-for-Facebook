package com.pitchedapps.frost.services

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.WebOverlayActivity
import com.pitchedapps.frost.dbflow.NotificationModel
import com.pitchedapps.frost.dbflow.lastNotificationTime
import com.pitchedapps.frost.dbflow.loadFbCookie
import com.pitchedapps.frost.dbflow.saveNotificationTime
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.utils.ARG_URL
import com.pitchedapps.frost.utils.L
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Created by Allan Wang on 2017-06-14.
 */
class NotificationService : IntentService(NotificationService::class.java.simpleName) {

    companion object {
        const val ARG_ID = "arg_id"
        val epochMatcher: Regex by lazy { Regex(":([0-9]*),") }
        val notifIdMatcher: Regex by lazy { Regex("notif_id\":([0-9]*),") }
    }

    override fun onHandleIntent(intent: Intent) {
        val id = intent.getLongExtra(ARG_ID, -1L)
        L.i("Handling notifications for $id")
        if (id == -1L) return
        val data = loadFbCookie(id) ?: return
        L.i("Using data $data")
        val doc = Jsoup.connect(FbTab.NOTIFICATIONS.url).cookie(FACEBOOK_COM, data.cookie).get()
        val unreadNotifications = doc.getElementById("notifications_list").getElementsByClass("aclb")
        var notifCount = 0
        var latestEpoch = lastNotificationTime(data.id)
        L.i("Latest Epoch $latestEpoch")
        unreadNotifications.forEach {
            elem ->
            val notif = parseNotification(data.id, elem)
            if (notif != null) {
                if (notif.timestamp <= latestEpoch) return@forEach
                notif.createNotification(this)
                latestEpoch = notif.timestamp
                notifCount++
            }
        }
        saveNotificationTime(NotificationModel(data.id, latestEpoch))
        summaryNotification(data.id, notifCount)
    }

    fun parseNotification(userId: Long, element: Element): NotificationContent? {
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
        return NotificationContent(userId, notifId.toInt(), a.attr("href"), text, epoch)
    }

    data class NotificationContent(val userId: Long, val notifId: Int, val href: String, val text: String, val timestamp: Long) {
        fun createNotification(context: Context) {
            val intent = Intent(context, WebOverlayActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or PendingIntent.FLAG_ONE_SHOT)
            intent.putExtra(ARG_URL, "$FB_URL_BASE$href")
            intent.action = System.currentTimeMillis().toString() //dummy action
            val bundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_right).toBundle()
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT, bundle)
            val notifBuilder = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.frost_f_24)
                    .setContentTitle(context.string(R.string.app_name))
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setGroup("frost_$userId")
                    .setAutoCancel(true)

            if (timestamp != -1L) notifBuilder.setWhen(timestamp * 1000)

            NotificationManagerCompat.from(context).notify("frost_$userId", notifId, notifBuilder.build())
        }
    }

    fun summaryNotification(userId: Long, count: Int) {
        if (count <= 1) return
        val notifBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.frost_f_24)
                .setContentTitle(string(R.string.app_name))
                .setContentText("$count notifications")
                .setGroup("frost_$userId")
                .setGroupSummary(true)
                .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify("frost_$userId", userId.toInt(), notifBuilder.build())
    }

    private fun log(element: Element) {
        with(element) {
            L.i("\n\nElement ${text()}")
            attributes().forEach {
                L.i("attr ${it.html()}")
            }
            L.i("Content ${html()}")
        }
    }

}