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

import android.os.Bundle
import android.view.View
import ca.allanwang.kau.utils.scaleXY
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.IntroActivity
import com.pitchedapps.frost.enums.Theme
import com.pitchedapps.frost.utils.Prefs
import kotlinx.android.synthetic.main.intro_theme.*

/**
 * Created by Allan Wang on 2017-07-28.
 */
class IntroFragmentTheme : BaseIntroFragment(R.layout.intro_theme) {

    val themeList
        get() = listOf(intro_theme_light, intro_theme_dark, intro_theme_amoled, intro_theme_glass)

    override fun viewArray(): Array<Array<out View>> = arrayOf(
        arrayOf(title),
        arrayOf(intro_theme_light, intro_theme_dark),
        arrayOf(intro_theme_amoled, intro_theme_glass)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intro_theme_light.setThemeClick(Theme.LIGHT)
        intro_theme_dark.setThemeClick(Theme.DARK)
        intro_theme_amoled.setThemeClick(Theme.AMOLED)
        intro_theme_glass.setThemeClick(Theme.GLASS)
        val currentTheme = Prefs.theme - 1
        if (currentTheme in 0..3)
            themeList.forEachIndexed { index, v ->
                v.scaleXY = if (index == currentTheme) 1.6f else 0.8f
            }
    }

    private fun View.setThemeClick(theme: Theme) {
        setOnClickListener { v ->
            Prefs.theme = theme.ordinal
            (activity as IntroActivity).apply {
                ripple.ripple(Prefs.bgColor, v.x + v.pivotX, v.y + v.pivotY)
                theme()
            }
            themeList.forEach { it.animate().scaleXY(if (it == this) 1.6f else 0.8f).start() }
        }
    }
}
