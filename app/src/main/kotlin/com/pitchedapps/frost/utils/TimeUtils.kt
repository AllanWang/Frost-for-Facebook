/*
 * Copyright 2019 Allan Wang
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
package com.pitchedapps.frost.utils

import android.content.Context
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Converts time in millis to readable date,
 * eg Apr 24 at 7:32 PM
 *
 * With regards to date modifications in calendars,
 * it appears to respect calendar rules;
 * see https://stackoverflow.com/a/43227817/4407321
 */
fun Long.toReadableTime(context: Context): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    val timeFormatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
    val time = timeFormatter.format(Date(this))
    val day = when {
        cal >= Calendar.getInstance().apply {
            add(
                Calendar.DAY_OF_MONTH,
                -1
            )
        } -> context.string(R.string.today)
        cal >= Calendar.getInstance().apply {
            add(
                Calendar.DAY_OF_MONTH,
                -2
            )
        } -> context.string(R.string.yesterday)
        else -> {
            val dayFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
            dayFormatter.format(Date(this))
        }
    }
    return context.getString(R.string.time_template, day, time)
}
