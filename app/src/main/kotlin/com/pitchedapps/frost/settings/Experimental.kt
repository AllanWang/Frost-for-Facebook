package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.R
import com.pitchedapps.frost.SettingsActivity
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-06-29.
 */
fun SettingsActivity.getExperimentalPrefs(): KPrefAdapterBuilder.() -> Unit = {

    plainText(R.string.experimental_disclaimer) {
        descRes = R.string.experimental_disclaimer_info
    }

    checkbox(R.string.search, { Prefs.searchBar }, { Prefs.searchBar = it; setResult(MainActivity.REQUEST_SEARCH) }) {
        descRes = R.string.search_desc
    }

    checkbox(R.string.verbose_logging, { Prefs.verboseLogging }, { Prefs.verboseLogging = it }) {
        descRes = R.string.verbose_logging_desc
    }

    plainText(R.string.restart_frost) {
        descRes = R.string.restart_frost_desc
        onClick = {
            _, _, _ ->
            setResult(MainActivity.REQUEST_RESTART_APPLICATION)
            finish()
            true
        }
    }
}