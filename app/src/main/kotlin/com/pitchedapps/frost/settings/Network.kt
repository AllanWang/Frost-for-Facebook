package com.pitchedapps.frost.settings

import ca.allanwang.kau.kpref.activity.KPrefAdapterBuilder
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-08-08.
 */
fun SettingsActivity.getNetworkPrefs(): KPrefAdapterBuilder.() -> Unit = {

    checkbox(R.string.network_media_on_metered, { Prefs.loadMediaOnMeteredNetwork }, { Prefs.loadMediaOnMeteredNetwork = it }) {
        descRes = R.string.network_media_on_metered_desc
    }

}