package com.pitchedapps.frost.intro

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by Allan Wang on 2017-07-25.
 */
class IntroPageAdapter(fm: FragmentManager, val fragments: List<() -> Fragment>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment = fragments[position]()

    override fun getCount(): Int = fragments.size

}