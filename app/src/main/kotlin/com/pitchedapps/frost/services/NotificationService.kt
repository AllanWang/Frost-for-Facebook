package com.pitchedapps.frost.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.loadFbCookiesSync
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
 * All fetching is done through parsers
 */
class NotificationService : JobService() {

    var future: Future<Unit>? = null

    val startTime = System.currentTimeMillis()

    override fun onStopJob(params: JobParameters?): Boolean {
        val time = System.currentTimeMillis() - startTime
        L.d { "Notification service has finished abruptly in $time ms" }
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
        L.i { "Notification service has finished in $time ms" }
        frostAnswersCustom("NotificationTime",
                "Type" to "Service",
                "IM Included" to Prefs.notificationsInstantMessages,
                "Duration" to time)
        jobFinished(params, false)
        future?.cancel(true)
        future = null
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        L.i { "Fetching notifications" }
        future = doAsync {
            val context = weakRef.get()
                    ?: return@doAsync L.eThrow("NotificationService had null weakRef to self")
            val currentId = Prefs.userId
            val cookies = loadFbCookiesSync()
            cookies.forEach {
                val current = it.id == currentId
                if (current || Prefs.notificationAllAccounts)
                    NotificationType.GENERAL.fetch(context, it)
                if (Prefs.notificationsInstantMessages
                        && (current || Prefs.notificationsImAllAccounts))
                    NotificationType.MESSAGE.fetch(context, it)
            }
            finish(params)
        }
        return true
    }

    private fun logNotif(text: String): NotificationContent? {
        L.eThrow("NotificationService: $text")
        return null
    }

    private fun Context.debugNotification(text: String = string(R.string.kau_lorem_ipsum)) {
        if (!BuildConfig.DEBUG) return
        val notifBuilder = frostNotification.withDefaults()
                .setContentTitle(string(R.string.frost_name))
                .setContentText(text)
        NotificationManagerCompat.from(this).notify(999, notifBuilder.build())
    }

}