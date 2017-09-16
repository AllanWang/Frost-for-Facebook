package com.pitchedapps.frost.enums

import android.webkit.WebView

/**
 * Created by Allan Wang on 2017-09-16.
 */
enum class UserAgents(val agent: String) {
    USER_AGENT_FULL("Mozilla/5.0 (Linux; Android 4.4.2; en-us; SAMSUNG SM-G900T Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/1.6 Chrome/28.0.1500.94 Mobile Safari/537.36"),
    USER_AGENT_BASIC("Mozilla/5.0 (BB10; Kbd) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.1.0.4633 Mobile Safari/537.10+"),
    USER_AGENT_MESSENGER("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");

    fun apply(webView: WebView) {
        webView.settings.apply {
            if (userAgentString != agent)
                userAgentString = agent
        }
    }
}