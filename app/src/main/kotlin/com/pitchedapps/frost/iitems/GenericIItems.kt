package com.pitchedapps.frost.iitems

import android.content.Context
import android.view.View
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
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
                            item.click(v.context)
                            true
                        } else
                            false
                    }
        }
    }

}

/**
 * IItem with just a textview.
 * Everything can be customized, but the textview must have id [R.id.item_text_view]
 */
open class TextIItem(val text: String?,
                     val textColor: Int = Prefs.textColor,
                     val bgColor: Int = Prefs.nativeBgColor,
                     layoutRes: Int = R.layout.iitem_text,
                     itemId: Int = R.layout.iitem_text)
    : KauIItem<TextIItem, TextIItem.ViewHolder>(layoutRes, ::ViewHolder, itemId) {

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<TextIItem>(itemView) {

        val text: TextView by bindView(R.id.item_text_view)

        override fun bindView(item: TextIItem, payloads: MutableList<Any>) {
            text.setTextColor(item.textColor)
            text.text = item.text
            text.setBackgroundColor(item.bgColor)
        }

        override fun unbindView(item: TextIItem) {
            text.text = null
        }
    }

}