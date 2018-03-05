package com.pitchedapps.frost.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.dbflow.CookieModel
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

    private var future: Future<Unit>? = null

    private val startTime = System.currentTimeMillis()

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
            val jobId = params?.extras?.getInt(NOTIFICATION_PARAM_ID, -1) ?: -1
            var notifCount = 0
            cookies.forEach {
                val current = it.id == currentId
                if (Prefs.notificationsGeneral
                        && (current || Prefs.notificationAllAccounts))
                    notifCount += NotificationType.GENERAL.fetch(context, it)
                if (Prefs.notificationsInstantMessages
                        && (current || Prefs.notificationsImAllAccounts))
                    notifCount += NotificationType.MESSAGE.fetch(context, it)
            }

            if (notifCount == 0 && jobId == NOTIFICATION_JOB_NOW)
                generalNotification(665, R.string.no_new_notifications, BuildConfig.DEBUG)

            finish(params)
        }
        return true
    }

    /**
     * Implemented fetch to also notify when an error occurs
     * Also normalized the output to return the number of notifications received
     */
    private fun fetch(jobId: Int, type: NotificationType, cookie: CookieModel): Int {
        val count = type.fetch(this, cookie)
        if (count < 0) {
            if (jobId == NOTIFICATION_JOB_NOW)
                generalNotification(666, R.string.error_notification, BuildConfig.DEBUG)
            return 0
        }
        return count
    }

    private fun logNotif(text: String): NotificationContent? {
        L.eThrow("NotificationService: $text")
        return null
    }

    private fun generalNotification(id: Int, textRes: Int, withDefaults: Boolean) {
        val notifBuilder = frostNotification.apply { if (withDefaults) withDefaults() }
                .setContentTitle(string(R.string.frost_name))
                .setContentText(string(textRes))
        NotificationManagerCompat.from(this).notify(id, notifBuilder.build())
    }

}