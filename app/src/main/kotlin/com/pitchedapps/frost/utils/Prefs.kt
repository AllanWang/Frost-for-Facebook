package com.pitchedapps.frost.utils

import android.graphics.Color
import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.StringSet
import ca.allanwang.kau.kpref.kpref
import ca.allanwang.kau.utils.lazyResettable
import com.pitchedapps.frost.injectors.InjectorContract

/**
 * Created by Allan Wang on 2017-05-28.
 *
 * Shared Preference object with lazy cached retrievals
 */
object Prefs : KPref() {

    var lastLaunch: Long by kpref("last_launch", -1L)

    var userId: Long by kpref("user_id", -1L)

    var prevId: Long by kpref("prev_id", -1L)

    var theme: Int by kpref("theme", 0, postSetter = { _: Int -> loader.invalidate() })

    var customTextColor: Int by kpref("color_text", Color.BLACK)

    var customBackgroundColor: Int by kpref("color_bg", 0xfffafafa.toInt())

    var customHeaderColor: Int by kpref("color_header", 0xff3b5998.toInt())

    var customIconColor: Int by kpref("color_icons", Color.WHITE)

    var exitConfirmation: Boolean by kpref("exit_confirmation", true)

    var notificationFreq: Long by kpref("notification_freq", -1L)

    var versionCode: Int by kpref("version_code", -1)

    var installDate: Long by kpref("install_date", -1L)

    var identifier: Int by kpref("identifier", -1)

    private val loader = lazyResettable { Theme.values[Prefs.theme] }

    private val t: Theme by loader

    val textColor: Int
        get() = t.textColor

    val bgColor: Int
        get() = t.bgColor

    val headerColor: Int
        get() = t.headerColor

    val iconColor: Int
        get() = t.iconColor

    val themeInjector: InjectorContract
        get() = t.injector

    val isCustomTheme: Boolean
        get() = t == Theme.CUSTOM

    var showRoundedIcons: Boolean by kpref("rounded_icons", true)

    var showSuggestedFriends: Boolean by kpref("suggested_friends_feed", true)

    var showFacebookAds: Boolean by kpref("facebook_ads", true)

    var animate: Boolean by kpref("fancy_animations", true)

    var notificationKeywords: StringSet by kpref("notification_keywords", mutableSetOf<String>())
}
