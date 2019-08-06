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

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import ca.allanwang.kau.utils.bindViewResettable
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.visible
import ca.allanwang.kau.utils.withAlpha
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.launchTabCustomizerActivity
import kotlin.math.abs

/**
 * Created by Allan Wang on 2017-07-28.
 */
abstract class BaseImageIntroFragment(
    val titleRes: Int,
    val imageRes: Int,
    val descRes: Int
) : BaseIntroFragment(R.layout.intro_image) {

    val imageDrawable: LayerDrawable by lazyResettableRegistered { image.drawable as LayerDrawable }
    val phone: Drawable by lazyResettableRegistered { imageDrawable.findDrawableByLayerId(R.id.intro_phone) }
    val screen: Drawable by lazyResettableRegistered { imageDrawable.findDrawableByLayerId(R.id.intro_phone_screen) }
    val icon: ImageView by bindViewResettable(R.id.intro_button)

    override fun viewArray(): Array<Array<out View>> = arrayOf(arrayOf(title), arrayOf(desc))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        title.setText(titleRes)
        image.setImageResource(imageRes)
        desc.setText(descRes)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun themeFragmentImpl() {
        super.themeFragmentImpl()
        title.setTextColor(Prefs.textColor)
        desc.setTextColor(Prefs.textColor)
        phone.tint(Prefs.textColor)
        screen.tint(Prefs.bgColor)
    }

    fun themeImageComponent(color: Int, vararg id: Int) {
        id.forEach { imageDrawable.findDrawableByLayerId(it).tint(color) }
    }

    override fun onPageScrolledImpl(positionOffset: Float) {
        super.onPageScrolledImpl(positionOffset)
        val alpha = ((1 - abs(positionOffset)) * 255).toInt()
        //apply alpha to all layers except the phone base
        (0 until imageDrawable.numberOfLayers).forEach {
            val d = imageDrawable.getDrawable(it)
            if (d != phone) d.alpha = alpha
        }
    }

    fun firstImageFragmentTransition(offset: Float) {
        if (offset < 0)
            image.alpha = 1 + offset
    }

    fun lastImageFragmentTransition(offset: Float) {
        if (offset > 0)
            image.alpha = 1 - offset
    }
}

class IntroAccountFragment : BaseImageIntroFragment(
    R.string.intro_multiple_accounts,
    R.drawable.intro_phone_nav,
    R.string.intro_multiple_accounts_desc
) {

    override fun themeFragmentImpl() {
        super.themeFragmentImpl()
        themeImageComponent(Prefs.iconColor, R.id.intro_phone_avatar_1, R.id.intro_phone_avatar_2)
        themeImageComponent(Prefs.bgColor.colorToForeground(), R.id.intro_phone_nav)
        themeImageComponent(Prefs.headerColor, R.id.intro_phone_header)
    }

    override fun onPageScrolledImpl(positionOffset: Float) {
        super.onPageScrolledImpl(positionOffset)
        firstImageFragmentTransition(positionOffset)
    }
}

class IntroTabTouchFragment : BaseImageIntroFragment(
    R.string.intro_easy_navigation, R.drawable.intro_phone_tab, R.string.intro_easy_navigation_desc
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        icon.visible().setIcon(GoogleMaterial.Icon.gmd_edit, 24)
        icon.setOnClickListener {
            activity?.launchTabCustomizerActivity()
        }
    }

    override fun themeFragmentImpl() {
        super.themeFragmentImpl()
        themeImageComponent(
            Prefs.iconColor,
            R.id.intro_phone_icon_1,
            R.id.intro_phone_icon_2,
            R.id.intro_phone_icon_3,
            R.id.intro_phone_icon_4
        )
        themeImageComponent(Prefs.headerColor, R.id.intro_phone_tab)
        themeImageComponent(Prefs.textColor.withAlpha(80), R.id.intro_phone_icon_ripple)
    }
}

class IntroTabContextFragment : BaseImageIntroFragment(
    R.string.intro_context_aware,
    R.drawable.intro_phone_long_press,
    R.string.intro_context_aware_desc
) {

    override fun themeFragmentImpl() {
        super.themeFragmentImpl()
        themeImageComponent(Prefs.headerColor, R.id.intro_phone_toolbar)
        themeImageComponent(Prefs.bgColor.colorToForeground(0.1f), R.id.intro_phone_image)
        themeImageComponent(
            Prefs.bgColor.colorToForeground(0.2f),
            R.id.intro_phone_like,
            R.id.intro_phone_share
        )
        themeImageComponent(Prefs.bgColor.colorToForeground(0.3f), R.id.intro_phone_comment)
        themeImageComponent(
            Prefs.bgColor.colorToForeground(0.1f),
            R.id.intro_phone_card_1,
            R.id.intro_phone_card_2
        )
        themeImageComponent(
            Prefs.textColor,
            R.id.intro_phone_image_indicator,
            R.id.intro_phone_comment_indicator,
            R.id.intro_phone_card_indicator
        )
    }

    override fun onPageScrolledImpl(positionOffset: Float) {
        super.onPageScrolledImpl(positionOffset)
        lastImageFragmentTransition(positionOffset)
    }
}
