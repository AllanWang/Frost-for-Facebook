package com.pitchedapps.frost.enums

import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-08-19.
 */
enum class MainActivityLayout(
    val titleRes: Int,
    val layoutRes: Int,
    val backgroundColor: () -> Int,
    val iconColor: () -> Int
) {

    TOP_BAR(R.string.top_bar,
        R.layout.activity_main,
        { Prefs.headerColor },
        { Prefs.iconColor }),

    BOTTOM_BAR(R.string.bottom_bar,
        R.layout.activity_main_bottom_tabs,
        { Prefs.bgColor },
        { Prefs.textColor });

    companion object {
        val values = values() //save one instance
        operator fun invoke(index: Int) = values[index]
    }
}