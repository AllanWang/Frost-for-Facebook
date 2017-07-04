package com.pitchedapps.frost.utils

import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.kpref

/**
 * Created by Allan Wang on 2017-07-03.
 *
 * Showcase prefs that offer one time helpers to guide new users
 */
object Showcase : KPref() {

    //check if this is the first time launching the web overlay; show snackbar if true
    var firstWebOverlay: Boolean by kpref("first_web_overlay", true)

    //not a showcase but cannot be in the same file as Prefs
    var experimentalDefault: Boolean by kpref("experimental_by_default", false)
}

