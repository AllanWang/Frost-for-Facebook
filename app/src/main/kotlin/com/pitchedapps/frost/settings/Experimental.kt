package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import ca.allanwang.kau.logging.KL
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.Showcase

/**
 * Created by Allan Wang on 2017-06-29.
 */
fun SettingsActivity.getExperimentalPrefs(): KPrefAdapterBuilder.() -> Unit = {

    plainText(R.string.experimental_disclaimer) {
        descRes = R.string.experimental_disclaimer_info
    }

    checkbox(R.string.experimental_by_default, { Showcase.experimentalDefault }, { Showcase.experimentalDefault = it }) {
        descRes = R.string.experimental_by_default_desc
    }

    // Experimental content starts here ------------------


    // Experimental content ends here --------------------

    checkbox(R.string.verbose_logging, { Prefs.verboseLogging }, {
        Prefs.verboseLogging = it
        KL.debug(it)
        KL.showPrivateText = false
        L.debug(it)
        KL.showPrivateText = false
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