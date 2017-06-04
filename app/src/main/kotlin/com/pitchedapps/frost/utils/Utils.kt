package com.pitchedapps.frost.utils

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by Allan Wang on 2017-05-28.
 */
object Utils {
    fun dpToPx(dp: Int) = (dp * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
    fun pxToDp(px:Int) = (px / android.content.res.Resources.getSystem().displayMetrics.density).toInt()

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
}