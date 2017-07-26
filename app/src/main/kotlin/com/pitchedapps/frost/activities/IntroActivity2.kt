package com.pitchedapps.frost.activities

import android.os.Bundle
import ca.allanwang.kau.ui.widgets.InkPageIndicator
import ca.allanwang.kau.utils.color
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.pitchedapps.frost.R


/**
 * Created by Allan Wang on 2017-07-25.
 */
class IntroActivity2 : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(R.color.facebook_blue)))
        addSlide(AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(R.color.colorAccent)))
        addSlide(AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(R.color.facebook_blue)))
        addSlide(AppIntroFragment.newInstance("test", "desc", R.drawable.frost_f_256, color(R.color.colorAccent)))
        setFlowAnimation()
    }
}