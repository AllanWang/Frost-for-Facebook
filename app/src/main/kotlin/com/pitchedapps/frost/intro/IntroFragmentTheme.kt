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

    override fun viewArray(): Array<Array<out View>> = arrayOf(arrayOf(title), arrayOf(intro_theme_light, intro_theme_dark), arrayOf(intro_theme_amoled, intro_theme_glass))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intro_theme_light.setThemeClick(Theme.LIGHT)
        intro_theme_dark.setThemeClick(Theme.DARK)
        intro_theme_amoled.setThemeClick(Theme.AMOLED)
        intro_theme_glass.setThemeClick(Theme.GLASS)
        val currentTheme = Prefs.theme - 1
        if (currentTheme in 0..3)
            themeList.forEachIndexed { index, v -> v.scaleXY = if (index == currentTheme) 1.6f else 0.8f }
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