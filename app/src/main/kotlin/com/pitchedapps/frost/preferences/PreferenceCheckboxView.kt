package com.pitchedapps.frost.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.support.annotation.ColorInt
import android.view.View

/**
 * Created by Allan Wang on 2017-06-06.
 */
class PreferenceCheckboxView(context: Context, builder: PrefItem<Boolean>, themeBuilder: ThemeBuilder?) : PreferenceView<Boolean>(context, builder, themeBuilder) {

    init {
        checkbox.visibility = View.VISIBLE
    }

    override fun viewWithClick() = checkbox

    override fun setAccentColor(@ColorInt color: Int) {
        val state = ColorStateList.valueOf(color)
        checkbox.buttonTintList = state
        icon.imageTintList = state
    }
}