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
package com.pitchedapps.frost.settings

import android.util.Log
import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.logging.KL
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.REQUEST_RESTART_APPLICATION

/**
 * Created by Allan Wang on 2017-06-29.
 */
fun SettingsActivity.getExperimentalPrefs(): KPrefAdapterBuilder.() -> Unit = {

    plainText(R.string.disclaimer) {
        descRes = R.string.experimental_disclaimer_info
    }

    // Experimental content starts here ------------------

    // Experimental content ends here --------------------

    checkbox(R.string.verbose_logging, Prefs::verboseLogging, {
        Prefs.verboseLogging = it
        KL.shouldLog = { it != Log.VERBOSE }
    }) {
        descRes = R.string.verbose_logging_desc
    }

    plainText(R.string.restart_frost) {
        descRes = R.string.restart_frost_desc
        onClick = {
            setFrostResult(REQUEST_RESTART_APPLICATION)
            finish()
        }
    }
}
