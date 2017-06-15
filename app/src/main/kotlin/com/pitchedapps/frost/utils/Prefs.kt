package com.pitchedapps.frost.utils

import android.graphics.Color
import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.kpref

/**
 * Created by Allan Wang on 2017-05-28.
 *
 * Shared Preference object with lazy cached retrievals
 */
object Prefs : KPref() {

    var lastActive: Long by kpref("last_active", -1L)

    var userId: Long by kpref("user_id", -1L)

    var theme: Int by kpref("theme", 0)

    var textColor: Int by kpref("color_text", Color.BLACK)

    var bgColor: Int by kpref("color_bg", Color.WHITE)

    var headerColor: Int by kpref("color_header", 0xff3b5998.toInt())

    var iconColor: Int by kpref("color_icons", Color.WHITE)

    var exitConfirmation: Boolean by kpref("exit_confirmation", true)
}
