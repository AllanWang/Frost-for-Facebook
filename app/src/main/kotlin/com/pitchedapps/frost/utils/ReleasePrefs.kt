package com.pitchedapps.frost.utils

import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.kpref
import com.pitchedapps.frost.BuildConfig

/**
 * Created by Allan Wang on 07/04/18.
 */
object ReleasePrefs : KPref() {
    var lastTimeStamp: Long by kpref("last_time_stamp", -1L)
    var enableUpdater: Boolean by kpref("enable_updater", BuildConfig.FLAVOR == FLAVOUR_GITHUB)
}