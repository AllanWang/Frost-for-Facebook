package com.pitchedapps.frost.utils

import android.content.Context
import ca.allanwang.kau.utils.copyToClipboard
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.formattedFbUrl

/**
 * Created by Allan Wang on 2017-07-07.
 */
fun Context.showWebContextMenu(url: String) {

    val formattedUrl = url.formattedFbUrl
    val contextItems = mutableListOf<WebContextType>()
    contextItems.add(WebContextType.COPY_URL)
    contextItems.add(WebContextType.COPY_URL)
    contextItems.add(WebContextType.COPY_URL)
    contextItems.add(WebContextType.COPY_URL)
    contextItems.add(WebContextType.COPY_URL)

    materialDialogThemed {
        title(formattedUrl)
        items(contextItems.map { this@showWebContextMenu.string(it.textId) })
        itemsCallback {
            dialog, itemView, position, text ->
            contextItems[position].onClick(this@showWebContextMenu, formattedUrl)
        }
    }
}

enum class WebContextType(val textId: Int, val onClick: (c: Context, url: String) -> Unit) {
    COPY_URL(R.string.copy_link, { c, url -> c.copyToClipboard(url) })
}