package com.pitchedapps.frost.web

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-05-31.
 */
class FrostChromeClient:WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        L.d("Console ${consoleMessage.lineNumber()}: ${consoleMessage.message()}")
        return super.onConsoleMessage(consoleMessage)
    }
}