package com.pitchedapps.frost.iitems

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.ui.createSimpleRippleDrawable
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.visible
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IClickable
import com.mikepenz.fastadapter.IItem
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.requests.MenuHeader
import com.pitchedapps.frost.facebook.requests.MenuItem
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.launchWebOverlay

/**
 * Created by Allan Wang on 30/12/17.
 */
abstract class MenuIItem<Item, VH : RecyclerView.ViewHolder>(layoutRes: Int, viewHolder: (v: View) -> VH)
    : KauIItem<Item, VH>(layoutRes, viewHolder) where Item : IItem<*, *>, Item : IClickable<*> {

    abstract val url: String?

    fun click(context: Context) {
        val url = url ?: return
        context.launchWebOverlay(url)
    }

    companion object {
        fun bindEvents(adapter: IAdapter<MenuIItem<*, *>>) {
            adapter.fastAdapter.withSelectable(false)
                    .withOnClickListener { v, _, item, _ ->
                        item.click(v.context)
                        true
                    }
        }
    }

}

class MenuHeaderIItem(val data: MenuHeader)
    : MenuIItem<MenuHeaderIItem, MenuHeaderIItem.ViewHolder>(R.layout.iitem_text, ::ViewHolder) {

    override val url: String? = null

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<MenuHeaderIItem>(itemView) {

        val text: TextView by bindView(R.id.item_text)

        override fun bindView(item: MenuHeaderIItem, payloads: MutableList<Any>) {
            text.setTextColor(Prefs.headerColor)
            text.text = item.data.header
        }

        override fun unbindView(item: MenuHeaderIItem) {
            text.text = null
        }
    }

}

class MenuContentIItem(val data: MenuItem)
    : MenuIItem<MenuContentIItem, MenuContentIItem.ViewHolder>(R.layout.iitem_menu, ::ViewHolder) {

    override val url: String?
        get() = data.url

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<MenuContentIItem>(itemView) {

        val frame: ViewGroup by bindView(R.id.item_frame)
        val icon: ImageView by bindView(R.id.item_icon)
        val content: TextView by bindView(R.id.item_content)
        val badge: TextView by bindView(R.id.item_badge)

        override fun bindView(item: MenuContentIItem, payloads: MutableList<Any>) {
            frame.background = createSimpleRippleDrawable(Prefs.textColor, Prefs.bgColor)
            content.setTextColor(Prefs.textColor)
            badge.setTextColor(Prefs.textColor)
            val iconUrl = item.data.pic
            if (iconUrl != null)
                Glide.with(itemView).load(iconUrl).into(icon.visible())
            else
                icon.gone()
            content.text = item.data.name
        }

        override fun unbindView(item: MenuContentIItem) {
            badge.gone()
        }
    }
}