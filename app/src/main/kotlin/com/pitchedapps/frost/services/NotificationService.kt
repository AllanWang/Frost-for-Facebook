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

import android.app.job.JobParameters
import androidx.core.app.NotificationManagerCompat
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.db.CookieDao
import com.pitchedapps.frost.db.CookieEntity
import com.pitchedapps.frost.db.NotificationDao
import com.pitchedapps.frost.db.selectAll
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostEvent
import com.pitchedapps.frost.widgets.NotificationWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * Created by Allan Wang on 2017-06-14.
 *
 * Service to manage notifications Will periodically check through all accounts in the db and send
 * notifications when appropriate
 *
 * All fetching is done through parsers
 */
@AndroidEntryPoint
class NotificationService : BaseJobService() {

  @Inject lateinit var prefs: Prefs

  @Inject lateinit var notifDao: NotificationDao

  @Inject lateinit var cookieDao: CookieDao

  override fun onStopJob(params: JobParameters?): Boolean {
    super.onStopJob(params)
    prepareFinish(true)
    return false
  }

  private var preparedFinish = false

  private fun prepareFinish(abrupt: Boolean) {
    if (preparedFinish) return
    preparedFinish = true
    val time = System.currentTimeMillis() - startTime
    L.i {
      "Notification service has ${if (abrupt) "finished abruptly" else "finished"} in $time ms"
    }
    frostEvent(
      "NotificationTime",
      "Type" to (if (abrupt) "Service force stop" else "Service"),
      "IM Included" to prefs.notificationsInstantMessages,
      "Duration" to time
    )
  }

  override fun onStartJob(params: JobParameters?): Boolean {
    super.onStartJob(params)
    L.i { "Fetching notifications" }
    launch {
      try {
        sendNotifications(params)
      } finally {
        if (!isActive) prepareFinish(false)
        jobFinished(params, false)
      }
    }
    return true
  }

  private suspend fun sendNotifications(params: JobParameters?): Unit =
    withContext(Dispatchers.Default) {
      val currentId = prefs.userId
      val cookies = cookieDao.selectAll()
      yield()
      val jobId = params?.extras?.getInt(NOTIFICATION_PARAM_ID, -1) ?: -1
      var notifCount = 0
      for (cookie in cookies) {
        yield()
        val current = cookie.id == currentId
        if (prefs.notificationsGeneral && (current || prefs.notificationAllAccounts))
          notifCount += fetch(jobId, NotificationType.GENERAL, cookie)
        if (prefs.notificationsInstantMessages && (current || prefs.notificationsImAllAccounts))
          notifCount += fetch(jobId, NotificationType.MESSAGE, cookie)
      }

      L.i { "Sent $notifCount notifications" }
      if (notifCount == 0 && jobId == NOTIFICATION_JOB_NOW)
        generalNotification(665, R.string.no_new_notifications, BuildConfig.DEBUG)
      if (notifCount > 0) {
        NotificationWidget.forceUpdate(this@NotificationService)
      }
    }

  /**
   * Implemented fetch to also notify when an error occurs Also normalized the output to return the
   * number of notifications received
   */
  private suspend fun fetch(jobId: Int, type: NotificationType, cookie: CookieEntity): Int {
    val count = type.fetch(this, cookie, prefs, notifDao)
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
    val notifBuilder =
      frostNotification(NOTIF_CHANNEL_GENERAL)
        .setFrostAlert(this, withDefaults, prefs.notificationRingtone, prefs)
        .setContentTitle(string(R.string.frost_name))
        .setContentText(string(textRes))
    NotificationManagerCompat.from(this).notify(id, notifBuilder.build())
  }
}
