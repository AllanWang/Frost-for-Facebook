package com.pitchedapps.frost.intro

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.utils.bindView
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-07-28.
 */
class IntroFragmentWelcome : BaseIntroFragment(R.layout.intro_welcome) {

    val welcome: TextView by bindView(R.id.intro_welcome)
    val frost: ImageView by bindView(R.id.intro_welcome_f)
    val slide: TextView by bindView(R.id.intro_welcome_slide)

    override fun viewArray(): Array<Array<out View>>
            = arrayOf(arrayOf(welcome), arrayOf(frost), arrayOf(slide))

    override fun themeFragment() {
        super.themeFragment()
        frost.imageTintList = ColorStateList.valueOf(Prefs.textColor)
    }

}