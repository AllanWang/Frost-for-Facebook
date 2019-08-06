/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.intro

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import ca.allanwang.kau.kotlin.LazyResettableRegistry
import ca.allanwang.kau.utils.Kotterknife
import ca.allanwang.kau.utils.bindViewResettable
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.setOnSingleTapListener
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.IntroActivity
import com.pitchedapps.frost.utils.Prefs
import kotlinx.android.synthetic.main.intro_analytics.*
import kotlin.math.abs

/**
 * Created by Allan Wang on 2017-07-28.
 *
 * Contains the base, start, and end fragments
 */

/**
 * The core intro fragment for all other fragments
 */
abstract class BaseIntroFragment(val layoutRes: Int) : Fragment() {

    val screenWidth
        get() = resources.displayMetrics.widthPixels

    val lazyRegistry = LazyResettableRegistry()

    protected fun translate(offset: Float, views: Array<Array<out View>>) {
        val maxTranslation = offset * screenWidth
        val increment = maxTranslation / views.size
        views.forEachIndexed { i, group ->
            group.forEach {
                it.translationX =
                    if (offset > 0) -maxTranslation + i * increment else -(i + 1) * increment
                it.alpha = 1 - abs(offset)
            }
        }
    }

    fun <T : Any> lazyResettableRegistered(initializer: () -> T) = lazyRegistry.lazy(initializer)

    /*
     * Note that these ids aren't actually inside all layouts
     * However, they are in most of them, so they are added here
     * for convenience
     */
    protected val title: TextView by bindViewResettable(R.id.intro_title)
    protected val image: ImageView by bindViewResettable(R.id.intro_image)
    protected val desc: TextView by bindViewResettable(R.id.intro_desc)

    protected fun defaultViewArray(): Array<Array<out View>> =
        arrayOf(arrayOf(title), arrayOf(image), arrayOf(desc))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutRes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        themeFragment()
    }

    override fun onDestroyView() {
        Kotterknife.reset(this)
        lazyRegistry.invalidateAll()
        super.onDestroyView()
    }

    fun themeFragment() {
        if (view != null) themeFragmentImpl()
    }

    protected open fun themeFragmentImpl() {
        (view as? ViewGroup)?.children?.forEach { (it as? TextView)?.setTextColor(Prefs.textColor) }
    }

    protected val viewArray: Array<Array<out View>> by lazyResettableRegistered { viewArray() }

    protected abstract fun viewArray(): Array<Array<out View>>

    fun onPageScrolled(positionOffset: Float) {
        if (view != null) onPageScrolledImpl(positionOffset)
    }

    protected open fun onPageScrolledImpl(positionOffset: Float) {
        translate(positionOffset, viewArray)
    }

    fun onPageSelected() {
        if (view != null) onPageSelectedImpl()
    }

    protected open fun onPageSelectedImpl() {
    }
}

class IntroFragmentWelcome : BaseIntroFragment(R.layout.intro_welcome) {

    override fun viewArray(): Array<Array<out View>> = defaultViewArray()

    override fun themeFragmentImpl() {
        super.themeFragmentImpl()
        image.imageTintList = ColorStateList.valueOf(Prefs.textColor)
    }
}

class IntroFragmentAnalytics : BaseIntroFragment(R.layout.intro_analytics) {

    val container: ConstraintLayout by bindViewResettable(R.id.intro_analytics_container)

    override fun viewArray(): Array<Array<out View>> = arrayOf(
        arrayOf(title), arrayOf(image),
        arrayOf(intro_switch), arrayOf(desc)
    )

    override fun themeFragmentImpl() {
        super.themeFragmentImpl()
        image.imageTintList = ColorStateList.valueOf(Prefs.textColor)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image.setIcon(GoogleMaterial.Icon.gmd_bug_report, 120)
        intro_switch.isSelected = Prefs.analytics
        intro_switch.setOnCheckedChangeListener { _, isChecked ->
            Prefs.analytics = isChecked
        }
    }
}

class IntroFragmentEnd : BaseIntroFragment(R.layout.intro_end) {

    val container: ConstraintLayout by bindViewResettable(R.id.intro_end_container)

    override fun viewArray(): Array<Array<out View>> = defaultViewArray()

    override fun themeFragmentImpl() {
        super.themeFragmentImpl()
        image.imageTintList = ColorStateList.valueOf(Prefs.textColor)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container.setOnSingleTapListener { _, event ->
            (activity as IntroActivity).finish(event.x, event.y)
        }
    }
}
