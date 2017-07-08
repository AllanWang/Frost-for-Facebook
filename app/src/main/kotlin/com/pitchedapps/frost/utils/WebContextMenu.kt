package com.pitchedapps.frost.utils

import android.content.Context
import ca.allanwang.kau.utils.copyToClipboard
import ca.allanwang.kau.utils.shareText
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.R

/**
 * Created by Allan Wang on 2017-07-07.
 */
fun Context.showWebContextMenu(wc: WebContext) {

    var title = wc.url
    title = title.substring(title.indexOf("m/") + 1) //just so if defaults to 0 in case it's not .com/
    if (title.length > 100) title = title.substring(0, 100) + '\u2026'

    materialDialogThemed {
        title(title)
        items(WebContextType.values.map { this@showWebContextMenu.string(it.textId) })
        itemsCallback {
            _, _, position, _ ->
            WebContextType[position].onClick(this@showWebContextMenu, wc)
        }
        dismissListener {
            //showing the dialog interrupts the touch down event, so we must ensure that the viewpager's swipe is enabled
            (this@showWebContextMenu as? MainActivity)?.viewPager?.enableSwipe = true
        }
    }
}

class WebContext(val url: String, val text: String)

enum class WebContextType(val textId: Int, val onClick: (c: Context, wc: WebContext) -> Unit) {
    COPY_LINK(R.string.copy_link, { c, wc -> c.copyToClipboard(wc.url) }),
    COPY_TEXT(R.string.copy_text, { c, wc -> c.copyToClipboard(wc.text) }),
    SHARE_LINK(R.string.share_link, { c, wc -> c.shareText(wc.url) })
    ;

    companion object {
        val values = values()
        operator fun get(index: Int) = values[index]
    }
}