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

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.utils.BiometricUtils
import com.pitchedapps.frost.utils.Prefs
import kotlinx.coroutines.launch

/**
 * Created by Allan Wang on 20179-05-01.
 */
fun SettingsActivity.getSecurityPrefs(): KPrefAdapterBuilder.() -> Unit = {

    plainText(R.string.disclaimer) {
        descRes = R.string.security_disclaimer_info
    }

    checkbox(R.string.enable_biometrics, Prefs::biometricsEnabled, {
        launch {
            /*
             * For security, we should request authentication when:
             * - enabling to ensure that it is supported
             * - disabling to ensure that it is permitted
             */
            BiometricUtils.authenticate(this@getSecurityPrefs, force = true).await()
            Prefs.biometricsEnabled = it
            reloadByTitle(R.string.enable_biometrics)
        }
    }) {
        descRes = R.string.enable_biometrics_desc
        enabler = { BiometricUtils.isSupported(this@getSecurityPrefs) }
    }
}
