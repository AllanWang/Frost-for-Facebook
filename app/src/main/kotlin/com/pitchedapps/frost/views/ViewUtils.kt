package com.pitchedapps.frost.views

import android.view.View
import android.view.ViewGroup

/**
 * Created by Allan Wang on 2017-05-31.
 */
fun View.matchParent() {
    with (layoutParams) {
        height = ViewGroup.LayoutParams.MATCH_PARENT
        width = ViewGroup.LayoutParams.MATCH_PARENT
    }
}