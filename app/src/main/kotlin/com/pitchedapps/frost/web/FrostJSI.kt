package com.pitchedapps.frost.web

import android.content.Context
import android.webkit.JavascriptInterface
import com.pitchedapps.frost.LoginActivity
import com.pitchedapps.frost.SelectorActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.utils.launchNewTask
import com.pitchedapps.frost.utils.launchWebOverlay

/**
 * Created by Allan Wang on 2017-06-01.
 */
class FrostJSI(val context: Context, val cookies: ArrayList<CookieModel>) {
    @JavascriptInterface
    fun loadUrl(url: String) = context.launchWebOverlay(url)

    @JavascriptInterface
    fun loadLogin() {
        if (cookies.isNotEmpty())
            context.launchNewTask(SelectorActivity::class.java, cookies)
        else
            context.launchNewTask(LoginActivity::class.java, clearStack = false)
    }

}