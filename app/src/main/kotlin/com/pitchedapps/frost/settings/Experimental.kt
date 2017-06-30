package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.KPrefAdapterBuilder
import com.pitchedapps.frost.R
import com.pitchedapps.frost.SettingsActivity
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-06-29.
 */
fun SettingsActivity.getExperimentalPrefs(): KPrefAdapterBuilder.() -> Unit = {
    checkbox(R.string.search, { Prefs.searchBar }, { Prefs.searchBar = it }) {
        descRes = R.string.search_desc
    }
}