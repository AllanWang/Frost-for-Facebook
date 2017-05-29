package com.pitchedapps.frost.utils

/**
 * Created by Allan Wang on 2017-05-28.
 */
class L {
    companion object {
        val TAG = "Frost"
        fun e(s: String) = android.util.Log.e(com.pitchedapps.frost.utils.L.Companion.TAG, s)
        fun d(s: String) = android.util.Log.d(com.pitchedapps.frost.utils.L.Companion.TAG, s)
    }
}