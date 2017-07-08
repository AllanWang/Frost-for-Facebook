package com.pitchedapps.frost.utils

import android.content.Context
import ca.allanwang.kau.utils.copyToClipboard
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R

/**
 * Created by Allan Wang on 2017-07-07.
 */
fun Context.showWebContextMenu(context: WebContext) {

    val contextItems = mutableListOf<WebContextType>()
    contextItems.add(WebContextType.COPY_LINK_ADDRESS)
    if (context.text != null)
        contextItems.add(WebContextType.COPY_LINK_TEXT)
    var title = context.url
    title = title.substring(title.indexOf("m/") + 1) //just so if defaults to 0 in case it's not .com/

    materialDialogThemed {
        title(title)
        items(contextItems.map { this@showWebContextMenu.string(it.textId) })
        itemsCallback {
            _, _, position, _ ->
            contextItems[position].onClick(this@showWebContextMenu, context)
        }
    }
}

class WebContext(val url: String, val text: String?)

enum class WebContextType(val textId: Int, val onClick: (c: Context, context: WebContext) -> Unit) {
    COPY_LINK_ADDRESS(R.string.copy_link_address, { c, content -> c.copyToClipboard(content.url) }),
    COPY_LINK_TEXT(R.string.copy_link_text, { c, content -> c.copyToClipboard(content.text!!) })
}