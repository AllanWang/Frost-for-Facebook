package com.pitchedapps.frost.utils

import com.pitchedapps.frost.R

/**
 * Created by Allan Wang on 2017-06-14.
 */
enum class Theme(val textRes: Int) {
    DEFAULT(R.string._default),
    LIGHT(R.string.light),
    DARK(R.string.dark),
    AMOLED(R.string.amoled),
    GLASS(R.string.glass),
    CUSTOM(R.string.custom);

    companion object {
        operator fun invoke(index: Int) = values()[index]
    }
}