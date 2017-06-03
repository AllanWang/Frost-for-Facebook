package com.pitchedapps.frost.events

import android.content.Context
import com.pitchedapps.frost.WebOverlayActivity

/**
 * Created by Allan Wang on 2017-06-01.
 */
class WebLaunchEvent(val url: String) {
    fun execute(context: Context) = WebOverlayActivity.newInstance(context, url)
}