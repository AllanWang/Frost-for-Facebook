package com.pitchedapps.frost.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-08-04.
 *
 * Cancels a notification
 */
private const val NOTIF_TAG_TO_CANCEL = "notif_tag_to_cancel"
private const val NOTIF_ID_TO_CANCEL = "notif_id_to_cancel"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        L.d("NotificationReceiver triggered")
        val notifTag = intent.getStringExtra(NOTIF_TAG_TO_CANCEL)
        val notifId = intent.getIntExtra(NOTIF_ID_TO_CANCEL, -1)
        if (notifId != -1) {
            L.d("NotificationReceiver: Cancelling $notifTag $notifId")
            NotificationManagerCompat.from(context).cancel(notifTag, notifId)
        }
    }
}

fun Context.getNotificationPendingCancelIntent(tag: String?, notifId: Int): PendingIntent {
    val cancelIntent = Intent(this, NotificationReceiver::class.java)
            .putExtra(NOTIF_TAG_TO_CANCEL, tag).putExtra(NOTIF_ID_TO_CANCEL, notifId)
    return PendingIntent.getBroadcast(this, 0, cancelIntent, 0)
}