package com.pitchedapps.frost.injectors

import android.webkit.WebView

/**
 * Created by Allan Wang on 2017-05-31.
 */
enum class CssHider(vararg val items: String) : InjectorContract {
    CORE("[data-sigil=\"m_login_upsell\"]"),
    HEADER("#header[data-sigil=\"MTopBlueBarHeader\"]", "#header-notices", "[data-sigil*=\"m-promo-jewel-header\"]"),
    ADS("[data-xt*=\"is_sponsored.1\"]")
    ;

    val injector: JsInjector by lazy { JsBuilder().css("${items.joinToString(separator = ",")}{display:none!important}").build() }

    override fun inject(webView: WebView, callback: ((String) -> Unit)?) {
        injector.inject(webView, callback)
    }

}