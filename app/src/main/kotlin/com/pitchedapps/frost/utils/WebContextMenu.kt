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
import ca.allanwang.kau.utils.shareText
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.toast
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.facebook.formattedFbUrl

/**
 * Created by Allan Wang on 2017-07-07.
 */
fun Context.showWebContextMenu(wc: WebContext) {

    var title = wc.url
    title = title.substring(title.indexOf("m/") + 1) //just so if defaults to 0 in case it's not .com/
    if (title.length > 100) title = title.substring(0, 100) + '\u2026'

    materialDialogThemed {
        title(title)
        items(WebContextType.values.map {
            if (it == WebContextType.COPY_TEXT && wc.text == null) return@map null
            this@showWebContextMenu.string(it.textId)
        }.filterNotNull())
        itemsCallback { _, _, position, _ ->
            WebContextType[position].onClick(this@showWebContextMenu, wc)
        }
        dismissListener {
            //showing the dialog interrupts the touch down event, so we must ensure that the viewpager's swipe is enabled
            (this@showWebContextMenu as? MainActivity)?.viewPager?.enableSwipe = true
        }
    }
}

class WebContext(val unformattedUrl: String, val text: String?) {
    val url = unformattedUrl.formattedFbUrl
}

enum class WebContextType(val textId: Int, val onClick: (c: Context, wc: WebContext) -> Unit) {
    OPEN_LINK(R.string.open_link, { c, wc -> c.launchWebOverlay(wc.unformattedUrl) }),
    COPY_LINK(R.string.copy_link, { c, wc -> c.copyToClipboard(wc.url) }),
    COPY_TEXT(
        R.string.copy_text,
        { c, wc -> if (wc.text != null) c.copyToClipboard(wc.text) else c.toast(R.string.no_text) }),
    SHARE_LINK(R.string.share_link, { c, wc -> c.shareText(wc.url) }),
    DEBUG_LINK(R.string.debug_link, { c, wc ->
        c.materialDialogThemed {
            title(R.string.debug_link)
            content(R.string.debug_link_desc)
            positiveText(R.string.kau_ok)
            onPositive { _, _ ->
                c.sendFrostEmail(R.string.debug_link_subject) {
                    message = c.string(R.string.debug_link_content)
                    addItem("Unformatted url", wc.unformattedUrl)
                    addItem("Formatted url", wc.url)
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
