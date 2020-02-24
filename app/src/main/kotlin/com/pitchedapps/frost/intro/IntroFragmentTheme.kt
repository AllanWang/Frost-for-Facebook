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
import com.pitchedapps.frost.databinding.IntroThemeBinding
import com.pitchedapps.frost.enums.Theme

/**
 * Created by Allan Wang on 2017-07-28.
 */
class IntroFragmentTheme : BaseIntroFragment(R.layout.intro_theme) {

    private lateinit var binding: IntroThemeBinding

    val themeList
        get() = with(binding) {
            listOf(introThemeLight, introThemeDark, introThemeAmoled, introThemeGlass)
        }

    override fun viewArray(): Array<Array<out View>> = with(binding) {
        arrayOf(
            arrayOf(title),
            arrayOf(introThemeLight, introThemeDark),
            arrayOf(introThemeAmoled, introThemeGlass)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = IntroThemeBinding.bind(view)
        binding.init()
    }

    private fun IntroThemeBinding.init() {
        introThemeLight.setThemeClick(Theme.LIGHT)
        introThemeDark.setThemeClick(Theme.DARK)
        introThemeAmoled.setThemeClick(Theme.AMOLED)
        introThemeGlass.setThemeClick(Theme.GLASS)
        val currentTheme = prefs.theme - 1
        if (currentTheme in 0..3)
            themeList.forEachIndexed { index, v ->
                v.scaleXY = if (index == currentTheme) 1.6f else 0.8f
            }
    }

    private fun View.setThemeClick(theme: Theme) {
        setOnClickListener { v ->
            prefs.theme = theme.ordinal
            (activity as IntroActivity).apply {
                binding.ripple.ripple(prefs.bgColor, v.x + v.pivotX, v.y + v.pivotY)
                theme()
            }
            themeList.forEach { it.animate().scaleXY(if (it == this) 1.6f else 0.8f).start() }
        }
    }
}
