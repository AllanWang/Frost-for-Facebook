package com.pitchedapps.frost.activities

import android.content.res.ColorStateList
import android.os.Bundle
import ca.allanwang.kau.imagepicker.ImagePickerActivityBase
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-07-23.
 */
class ImagePickerActivity : ImagePickerActivityBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fab.backgroundTintList = ColorStateList.valueOf(Prefs.iconBackgroundColor)
    }
}