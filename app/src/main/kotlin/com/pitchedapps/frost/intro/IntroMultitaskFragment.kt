package com.pitchedapps.frost.intro

import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-07-28.
 */
class IntroMultitaskFragment : BaseImageIntroFragment(
        R.string.intro_true_multitasking, R.drawable.intro_phone_nav, R.string.intro_true_multitasking_desc
) {

    override fun themeFragment() {
        super.themeFragment()
        themeImageComponent(Prefs.iconColor, R.id.intro_phone_avatar_1, R.id.intro_phone_avatar_2)
        themeImageComponent(Prefs.bgColor.colorToForeground(), R.id.intro_phone_nav)
        themeImageComponent(Prefs.headerColor, R.id.intro_phone_header)
    }
}