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
package com.pitchedapps.frost.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.toDrawable
import ca.allanwang.kau.utils.visible
import ca.allanwang.kau.utils.withAlpha
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.databinding.ViewBadgedIconBinding
import com.pitchedapps.frost.prefs.Prefs
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Allan Wang on 2017-06-19.
 */
class BadgedIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent {

    private val prefs: Prefs by inject()
    private val binding: ViewBadgedIconBinding =
        ViewBadgedIconBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.init()
    }

    private fun ViewBadgedIconBinding.init() {
        val badgeColor =
            prefs.mainActivityLayout.backgroundColor(prefs).withAlpha(255).colorToForeground(0.2f)
        val badgeBackground =
            GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(badgeColor, badgeColor)
            )
        badgeBackground.cornerRadius = 13.dpToPx.toFloat()
        badgeText.background = badgeBackground
        badgeText.setTextColor(prefs.mainActivityLayout.iconColor(prefs))
    }

    var iicon: IIcon? = null
        set(value) {
            field = value
            binding.badgeImage.setImageDrawable(
                value?.toDrawable(
                    context,
                    sizeDp = 20,
                    color = prefs.mainActivityLayout.iconColor(prefs)
                )
            )
        }

    fun setAllAlpha(alpha: Float) {
        // badgeTextView.setTextColor(prefs.textColor.withAlpha(alpha.toInt()))
        binding.badgeImage.drawable.alpha = alpha.toInt()
    }

    var badgeText: String?
        get() = binding.badgeText.text.toString()
        set(value) {
            with(binding) {
                if (badgeText.text == value) return
                badgeText.text = value
                if (value != null && value != "0") badgeText.visible()
                else badgeText.gone()
            }
        }
}
