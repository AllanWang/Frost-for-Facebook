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
import com.pitchedapps.frost.facebook.FB_URL_BASE

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Collection of short js functions that are embedded directly
 */
enum class JsActions(body: String) : InjectorContract {
    /**
     * Redirects to login activity if create account is found
     * see [com.pitchedapps.frost.web.FrostJSI.loadLogin]
     */
    LOGIN_CHECK("document.getElementById('signup-button')&&Frost.loadLogin();"),
    BASE_HREF("""document.write("<base href='$FB_URL_BASE'/>");"""),
    FETCH_BODY("""setTimeout(function(){var e=document.querySelector("main");e||(e=document.querySelector("body")),Frost.handleHtml(e.outerHTML)},1e2);"""),
    RETURN_BODY("return(document.getElementsByTagName('html')[0].innerHTML);"),
    CREATE_POST(clickBySelector("[role=textbox][onclick]")),
//    CREATE_MSG(clickBySelector("a[rel=dialog]")),
    /**
     * Used as a pseudoinjector for maybe functions
     */
    EMPTY("");

    val function = "(function(){$body})();"

    override fun inject(webView: WebView) =
        JsInjector(function).inject(webView)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun clickBySelector(selector: String): String =
    """document.querySelector("$selector").click()"""
