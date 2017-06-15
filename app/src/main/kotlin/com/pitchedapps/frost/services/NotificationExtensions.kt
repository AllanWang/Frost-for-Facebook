package com.pitchedapps.frost.services

import android.content.Context
import android.content.Intent

/**
 * Created by Allan Wang on 2017-06-14.
 */
fun Context.requestNotifications(id: Long) {
    val intent = Intent(this, NotificationService::class.java)
    intent.putExtra(NotificationService.ARG_ID, id)
    startService(intent)
}