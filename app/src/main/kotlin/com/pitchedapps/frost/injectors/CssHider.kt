/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.injectors

import android.webkit.WebView
import com.pitchedapps.frost.prefs.Prefs

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * List of elements to hide
 */
enum class CssHider(private vararg val items: String) : InjectorContract {
    CORE("[data-sigil=m_login_upsell]", "[role=progressbar]"),
    HEADER(
        "#header:not(.mFuturePageHeader):not(.titled)",
        "#mJewelNav",
        "[data-sigil=MTopBlueBarHeader]",
        "#header-notices",
        "[data-sigil*=m-promo-jewel-header]"
    ),
    ADS(
        "article[data-xt*=sponsor]",
        "article[data-store*=sponsor]"
    ),
    PEOPLE_YOU_MAY_KNOW("article._d2r"),
    SUGGESTED_GROUPS("article[data-ft*=\"ei\":]"),
    COMPOSER("#MComposer"),
    MESSENGER("._s15", "[data-testid=info_panel]", "js_i"),
    NON_RECENT("article:not([data-store*=actor_name])"),
    STORIES(
        "#MStoriesTray",
        // Sub element with just the tray; title is not a part of this
        "[data-testid=story_tray]"
    ),
    POST_ACTIONS(
        "footer [data-sigil=\"ufi-inline-actions\"]"
    ),
    POST_REACTIONS(
        "footer [data-sigil=\"reactions-bling-bar\"]"
    )
    ;

    val injector: JsInjector by lazy {
        JsBuilder().css("${items.joinToString(separator = ",")}{display:none !important}")
            .single("css-hider-$name").build()
    }

    override fun inject(webView: WebView, prefs: Prefs) =
        injector.inject(webView, prefs)
}
