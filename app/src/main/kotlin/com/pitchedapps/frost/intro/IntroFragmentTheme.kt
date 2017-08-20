package com.pitchedapps.frost.intro

import android.os.Bundle
import android.view.View
import ca.allanwang.kau.utils.bindViewResettable
import ca.allanwang.kau.utils.scaleXY
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.IntroActivity
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.enums.Theme

/**
 * Created by Allan Wang on 2017-07-28.
 */
class IntroFragmentTheme : BaseIntroFragment(R.layout.intro_theme) {

    val light: View by bindViewResettable(R.id.intro_theme_light)
    val dark: View by bindViewResettable(R.id.intro_theme_dark)
    val amoled: View by bindViewResettable(R.id.intro_theme_amoled)
    val glass: View by bindViewResettable(R.id.intro_theme_glass)

    val themeList
        get() = listOf(light, dark, amoled, glass)

    override fun viewArray(): Array<Array<out View>>
            = arrayOf(arrayOf(title), arrayOf(light, dark), arrayOf(amoled, glass))

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        light.setThemeClick(Theme.LIGHT)
        dark.setThemeClick(Theme.DARK)
        amoled.setThemeClick(Theme.AMOLED)
        glass.setThemeClick(Theme.GLASS)
        val currentTheme = Prefs.theme - 1
        if (currentTheme in 0..3)
            themeList.forEachIndexed { index, v -> v.scaleXY = if (index == currentTheme) 1.6f else 0.8f }
    }

    private fun View.setThemeClick(theme: Theme) {
        setOnClickListener {
            v ->
            Prefs.theme = theme.ordinal
            (activity as IntroActivity).apply {
                ripple.ripple(Prefs.bgColor, v.x + v.pivotX, v.y + v.pivotY)
                theme()
            }
            themeList.forEach { it.animate().scaleXY(if (it == this) 1.6f else 0.8f).start() }
        }
    }

}