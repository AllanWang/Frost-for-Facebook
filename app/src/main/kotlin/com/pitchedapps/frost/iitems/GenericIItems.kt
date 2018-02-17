package com.pitchedapps.frost.iitems

import android.content.Context
import android.view.View
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.ui.createSimpleRippleDrawable
import ca.allanwang.kau.utils.bindView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IItem
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.launchWebOverlay

/**
 * Created by Allan Wang on 30/12/17.
 */

/**
 * Base contract for anything with a url that may be launched in a new overlay
 */
interface ClickableIItemContract {

    val url: String?

    fun click(context: Context) {
        val url = url ?: return
        context.launchWebOverlay(url)
    }

    companion object {
        fun bindEvents(adapter: IAdapter<IItem<*, *>>) {
            adapter.fastAdapter.withSelectable(false)
                    .withOnClickListener { v, _, item, _ ->
                        if (item is ClickableIItemContract) {
                            item.click(v!!.context)
                            true
                        } else
                            false
                    }
        }
    }

}

/**
 * Generic header item
 * Not clickable with an accent color
 */
open class HeaderIItem(val text: String?,
                       itemId: Int = R.layout.iitem_header)
    : KauIItem<HeaderIItem, HeaderIItem.ViewHolder>(R.layout.iitem_header, ::ViewHolder, itemId) {

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<HeaderIItem>(itemView) {

        val text: TextView by bindView(R.id.item_header_text)

        override fun bindView(item: HeaderIItem, payloads: MutableList<Any>) {
            text.setTextColor(Prefs.accentColor)
            text.text = item.text
            text.setBackgroundColor(Prefs.nativeBgColor)
        }

        override fun unbindView(item: HeaderIItem) {
            text.text = null
        }
    }

}

/**
 * Generic text item
 * Clickable with text color
 */
open class TextIItem(val text: String?,
                     override val url: String?,
                     itemId: Int = R.layout.iitem_text)
    : KauIItem<TextIItem, TextIItem.ViewHolder>(R.layout.iitem_text, ::ViewHolder, itemId),
        ClickableIItemContract {

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<TextIItem>(itemView) {

        val text: TextView by bindView(R.id.item_text_view)

        override fun bindView(item: TextIItem, payloads: MutableList<Any>) {
            text.setTextColor(Prefs.textColor)
            text.text = item.text
            text.background = createSimpleRippleDrawable(Prefs.bgColor, Prefs.nativeBgColor)
        }

        override fun unbindView(item: TextIItem) {
            text.text = null
        }
    }

}