package com.pitchedapps.frost.intro

import android.support.v4.app.Fragment

/**
 * Created by Allan Wang on 2017-07-28.
 */
abstract class BaseIntroFragment : Fragment() {

    abstract fun onPageScrolled(positionOffset: Float, positionOffsetPixels: Int)

    abstract fun onPageSelected()

}