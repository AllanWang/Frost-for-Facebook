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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Receiver that is triggered whenever the app updates so it can bind the notifications again
 */
@AndroidEntryPoint
class UpdateReceiver : BroadcastReceiver() {

  @Inject lateinit var prefs: Prefs

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
    L.d { "Frost has updated" }
    context.scheduleNotifications(prefs.notificationFreq) // Update notifications
  }
}
