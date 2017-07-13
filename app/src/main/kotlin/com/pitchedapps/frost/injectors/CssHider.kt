package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * List of elements to hide
 */
enum class CssHider(vararg val items: String) : InjectorContract {
    CORE("[data-sigil=m_login_upsell]"),
    HEADER("#header[data-sigil=MTopBlueBarHeader]", "#header-notices", "[data-sigil*=m-promo-jewel-header]"),
    ADS(
            "article[data-xt*=sponsor]",
            "article[data-store*=sponsor]"
    ),
    PEOPLE_YOU_MAY_KNOW("article._d2r"),
    MESSENGER("._s15", "[data-testid=info_panel]", "js_i")
    ;

    val injector: JsInjector by lazy { JsBuilder().css("${items.joinToString(separator = ",")}{display:none!important}").build() }

    override fun inject(webView: WebView, callback: ((String) -> Unit)?) {
        injector.inject(webView, callback)
    }

}