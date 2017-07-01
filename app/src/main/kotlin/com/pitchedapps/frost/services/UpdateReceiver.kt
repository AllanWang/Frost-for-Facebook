package com.pitchedapps.frost.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.scheduleNotifications

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Receiver that is triggered whenever the app updates so it can bind the notifications again
 */
class UpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        L.d("Frost has updated")
        context.scheduleNotifications(Prefs.notificationFreq) //Update notifications
    }

}