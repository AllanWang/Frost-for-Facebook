package com.pitchedapps.frost.utils

import android.os.Bundle
import android.support.v4.app.Fragment
import com.pitchedapps.frost.fragments.BaseFragment

/**
 * Created by Allan Wang on 2017-05-29.
 */

private fun Fragment.bundle(): Bundle {
    if (this.arguments == null)
        this.arguments = Bundle()
    return this.arguments
}

fun <T : Fragment> T.putString(key: String, value: String): T {
    this.bundle().putString(key, value)
    return this
}

fun <T : Fragment> T.putInt(key: String, value: Int): T {
    this.bundle().putInt(key, value)
    return this
}