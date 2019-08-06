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
import androidx.constraintlayout.widget.ConstraintLayout
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.dpToPx
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.toDrawable
import ca.allanwang.kau.utils.visible
import ca.allanwang.kau.utils.withAlpha
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs
import kotlinx.android.synthetic.main.view_badged_icon.view.*

/**
 * Created by Allan Wang on 2017-06-19.
 */
class BadgedIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.view_badged_icon, this)
        val badgeColor =
            Prefs.mainActivityLayout.backgroundColor().withAlpha(255).colorToForeground(0.2f)
        val badgeBackground =
            GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(badgeColor, badgeColor)
            )
        badgeBackground.cornerRadius = 13.dpToPx.toFloat()
        badge_text.background = badgeBackground
        badge_text.setTextColor(Prefs.mainActivityLayout.iconColor())
    }

    var iicon: IIcon? = null
        set(value) {
            field = value
            badge_image.setImageDrawable(
                value?.toDrawable(
                    context,
                    sizeDp = 20,
                    color = Prefs.mainActivityLayout.iconColor()
                )
            )
        }

    fun setAllAlpha(alpha: Float) {
        //badgeTextView.setTextColor(Prefs.textColor.withAlpha(alpha.toInt()))
        badge_image.drawable.alpha = alpha.toInt()
    }

    var badgeText: String?
        get() = badge_text.text.toString()
        set(value) {
            if (badge_text.text == value) return
            badge_text.text = value
            if (value != null && value != "0") badge_text.visible()
            else badge_text.gone()
        }
}
