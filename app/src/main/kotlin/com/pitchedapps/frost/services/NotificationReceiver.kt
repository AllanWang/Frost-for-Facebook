package com.pitchedapps.frost.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by Allan Wang on 2017-05-31.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION = "com.pitchedapps.frost.NOTIFICATIONS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
    }

}