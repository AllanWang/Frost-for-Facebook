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
package com.pitchedapps.frost.utils

import android.content.Context
import ca.allanwang.kau.utils.copyToClipboard
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.shareText
import ca.allanwang.kau.utils.string
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.listItems
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.prefs.Prefs

/**
 * Created by Allan Wang on 2017-07-07.
 */
fun Context.showWebContextMenu(wc: WebContext, fbCookie: FbCookie) {
    if (wc.isEmpty) return
    var title = wc.url ?: string(R.string.menu)
    title =
        title.substring(title.indexOf("m/") + 1) // just so if defaults to 0 in case it's not .com/
    if (title.length > 100) title = title.substring(0, 100) + '\u2026'

    val menuItems = WebContextType.values
        .filter { it.constraint(wc) }

    materialDialog {
        title(text = title)
        listItems(items = menuItems.map { string(it.textId) }) { _, position, _ ->
            menuItems[position].onClick(this@showWebContextMenu, wc, fbCookie)
        }
        onDismiss {
            // showing the dialog interrupts the touch down event, so we must ensure that the viewpager's swipe is enabled
            (this@showWebContextMenu as? MainActivity)
                ?.contentBinding
                ?.viewpager
                ?.enableSwipe = true
        }
    }
}

class WebContext(val unformattedUrl: String?, val text: String?) {
    val url: String? = unformattedUrl?.formattedFbUrl
    inline val hasUrl get() = unformattedUrl != null
    inline val hasText get() = text != null
    inline val isEmpty get() = !hasUrl && !hasText
}

enum class WebContextType(
    val textId: Int,
    val constraint: (wc: WebContext) -> Boolean,
    val onClick: (c: Context, wc: WebContext, fc: FbCookie) -> Unit
) {
    OPEN_LINK(
        R.string.open_link,
        { it.hasUrl },
        { c, wc, fc -> c.launchWebOverlay(wc.url!!, fc, Prefs.get()) }),
    COPY_LINK(R.string.copy_link, { it.hasUrl }, { c, wc, _ -> c.copyToClipboard(wc.url) }),
    COPY_TEXT(R.string.copy_text, { it.hasText }, { c, wc, _ -> c.copyToClipboard(wc.text) }),
    SHARE_LINK(R.string.share_link, { it.hasUrl }, { c, wc, _ -> c.shareText(wc.url) }),
    DEBUG_LINK(R.string.debug_link, { it.hasUrl }, { c, wc, _ ->
        c.materialDialog {
            title(R.string.debug_link)
            message(R.string.debug_link_desc)
            positiveButton(R.string.kau_ok) {
                c.sendFrostEmail(R.string.debug_link_subject) {
                    message = c.string(R.string.debug_link_content)
                    addItem("Unformatted url", wc.unformattedUrl!!)
                    addItem("Formatted url", wc.url!!)
                }
            }
        }
    })
    ;

    companion object {
        val values = values()
        operator fun get(index: Int) = values[index]
    }
}
