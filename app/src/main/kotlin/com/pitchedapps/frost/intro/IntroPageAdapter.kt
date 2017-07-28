package com.pitchedapps.frost.intro

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup

/**
 * Created by Allan Wang on 2017-07-25.
 */
class IntroPageAdapter(fm: FragmentManager, val fragments: List<BaseIntroFragment>) : FragmentPagerAdapter(fm) {

    val retainedFragments: MutableMap<Int, BaseIntroFragment> = mutableMapOf()

    operator fun get(index: Int) = retainedFragments[index]

    override fun getItem(position: Int): Fragment = retainedFragments.getOrDefault(position, fragments[position])

    override fun getCount(): Int = fragments.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as BaseIntroFragment
        retainedFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        retainedFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }
}