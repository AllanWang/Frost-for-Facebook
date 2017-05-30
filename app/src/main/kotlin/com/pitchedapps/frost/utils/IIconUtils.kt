package com.pitchedapps.frost.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon

/**
 * Created by Allan Wang on 2017-05-29.
 */
fun IIcon.toDrawable(c: Context, sizeDp: Int = 24, @ColorRes color: Int = android.R.color.white): Drawable
        = IconicsDrawable(c).icon(this).colorRes(color).sizeDp(sizeDp)