package com.pitchedapps.frost.web

import android.webkit.JavascriptInterface
import com.pitchedapps.frost.events.WebLaunchEvent
import org.greenrobot.eventbus.EventBus

/**
 * Created by Allan Wang on 2017-06-01.
 */
class FrostJSI {
    @JavascriptInterface
    fun loadUrl(url: String) = EventBus.getDefault().post(WebLaunchEvent(url))
}