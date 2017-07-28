package com.pitchedapps.frost.intro

import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-07-28.
 */
abstract class BaseImageIntroFragment(val titleRes: Int, val imageRes: Int, val descRes: Int) : BaseIntroFragment(R.layout.intro_image) {

    val title: TextView by bindView(R.id.intro_title)
    val image: ImageView by bindView(R.id.intro_image)
    val desc: TextView by bindView(R.id.intro_desc)

    override fun viewArray(): Array<Array<out View>>
            = arrayOf(arrayOf(title), arrayOf(image), arrayOf(desc))

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        title.setText(titleRes)
        image.setImageResource(imageRes)
        desc.setText(descRes)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun themeFragment() {
        super.themeFragment()
        title.setTextColor(Prefs.textColor)
        desc.setTextColor(Prefs.textColor)
        themeImageComponent(Prefs.textColor, R.id.intro_phone)
        themeImageComponent(Prefs.bgColor.withAlpha(255), R.id.intro_phone_screen)
    }

    fun themeImageComponent(color: Int, vararg id: Int) {
        val layers = image.drawable as LayerDrawable
        id.forEach { layers.findDrawableByLayerId(it).tint(color) }
    }
}