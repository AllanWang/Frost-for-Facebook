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
import ca.allanwang.kau.kotlin.lazyContext
import com.pitchedapps.frost.utils.L
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.util.Locale

/**
 * Created by Allan Wang on 2017-05-31.
 * Mapping of the available assets
 * The enum name must match the css file name
 */
enum class JsAssets : InjectorContract {
    MENU, MENU_DEBUG, CLICK_A, CONTEXT_A, MEDIA, HEADER_BADGES, HEADER_HIDER, TEXTAREA_LISTENER, NOTIF_MSG,
    DOCUMENT_WATCHER
    ;

    var file = "${name.toLowerCase(Locale.CANADA)}.js"
    var injector = lazyContext {
        try {
            val content = it.assets.open("js/$file").bufferedReader().use(BufferedReader::readText)
            JsBuilder().js(content).single(name).build()
        } catch (e: FileNotFoundException) {
            L.e(e) { "JsAssets file not found" }
            JsInjector(JsActions.EMPTY.function)
        }
    }

    override fun inject(webView: WebView, callback: (() -> Unit)?) {
        injector(webView.context).inject(webView, callback)
    }
}
