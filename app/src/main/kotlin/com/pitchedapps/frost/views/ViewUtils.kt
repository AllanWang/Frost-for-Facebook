package com.pitchedapps.frost.views

import android.content.res.ColorStateList
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar


/**
 * Created by Allan Wang on 2017-05-31.
 */
fun View.matchParent() {
    with(layoutParams) {
        height = ViewGroup.LayoutParams.MATCH_PARENT
        width = ViewGroup.LayoutParams.MATCH_PARENT
    }
}

fun ProgressBar.tintRes(@ColorRes id: Int) = tint(ContextCompat.getColor(context, id))

fun ProgressBar.tint(@ColorInt color: Int) {
    val sl = ColorStateList.valueOf(color)
    progressTintList = sl
    secondaryProgressTintList = sl
    indeterminateTintList = sl
}

fun View.snackbar(text: String, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, text, duration).show()
}

fun View.snackbar(@StringRes textId: Int, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, textId, duration).show()
}