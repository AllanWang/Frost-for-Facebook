package com.pitchedapps.frost.utils

import android.graphics.Color
import com.pitchedapps.frost.R
import com.pitchedapps.frost.injectors.CssAssets
import com.pitchedapps.frost.injectors.InjectorContract
import com.pitchedapps.frost.injectors.JsActions

/**
 * Created by Allan Wang on 2017-06-14.
 */
enum class Theme(val textRes: Int, val injector: InjectorContract,
                 private val textColorGetter: () -> Int, private val backgroundColorGetter: () -> Int,
                 private val headerColorGetter: () -> Int, private val iconColorGetter: () -> Int) {
    DEFAULT(R.string.kau_default, JsActions.EMPTY, { Color.BLACK }, { 0xfffafafa.toInt() }, { 0xff3b5998.toInt() }, { Color.WHITE }),
    LIGHT(R.string.kau_light, CssAssets.MATERIAL_LIGHT, { Color.BLACK }, { 0xfffafafa.toInt() }, { 0xff3b5998.toInt() }, { Color.WHITE }),
    DARK(R.string.kau_dark, CssAssets.MATERIAL_DARK, { Color.WHITE }, { 0xff303030.toInt() }, { 0xff3b5998.toInt() }, { Color.WHITE }),
    AMOLED(R.string.kau_amoled, CssAssets.MATERIAL_AMOLED, { Color.WHITE }, { Color.BLACK }, { Color.BLACK }, { Color.WHITE }),
    GLASS(R.string.kau_glass, CssAssets.MATERIAL_GLASS, { Color.WHITE }, { 0x80000000.toInt() }, { 0xb3000000.toInt() }, { Color.WHITE }),
    CUSTOM(R.string.kau_custom, JsActions.EMPTY, { Prefs.customTextColor }, { Prefs.customBackgroundColor }, { Prefs.customHeaderColor }, { Prefs.customIconColor })
    ;

    val textColor: Int
        get() = textColorGetter.invoke()

    val bgColor: Int
        get() = backgroundColorGetter.invoke()

    val headerColor: Int
        get() = headerColorGetter.invoke()

    val iconColor: Int
        get() = iconColorGetter.invoke()

    companion object {
        val values = values() //save one instance
        operator fun invoke(index: Int) = values[index]
    }
}