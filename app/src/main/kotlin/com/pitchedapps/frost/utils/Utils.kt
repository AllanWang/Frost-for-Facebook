package com.pitchedapps.frost.utils

import android.content.res.Resources

/**
 * Created by Allan Wang on 2017-05-28.
 */
object Utils {
    fun dpToPx(dp: Int) = (dp * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
    fun pxToDp(px:Int) = (px / android.content.res.Resources.getSystem().displayMetrics.density).toInt()
}