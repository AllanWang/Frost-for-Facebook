package com.pitchedapps.frost.contracts

import android.view.View
import android.widget.TextView

/**
 * Created by Allan Wang on 2017-11-07.
 *
 * Should be implemented by all views in [com.pitchedapps.frost.activities.MainActivity]
 * to allow for instant view reloading
 */
interface FrostThemable {

    /**
     * Change all necessary view components to the new theme
     * and call whatever other children that also implement [FrostThemable]
     */
    fun reloadTheme()

    fun setTextColors(color: Int, vararg textViews: TextView?) =
            themeViews(color, *textViews) { setTextColor(it) }

    fun setBackgrounds(color: Int, vararg views: View?) =
            themeViews(color, *views) { setBackgroundColor(it) }

    fun <T : View> themeViews(color: Int, vararg views: T?, action: T.(Int) -> Unit) =
            views.filterNotNull().forEach { it.action(color) }

}