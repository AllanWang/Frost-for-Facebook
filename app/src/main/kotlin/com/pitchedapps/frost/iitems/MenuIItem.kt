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
package com.pitchedapps.frost.iitems

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.ui.createSimpleRippleDrawable
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.visible
import com.mikepenz.fastadapter.FastAdapter
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.requests.MenuFooterItem
import com.pitchedapps.frost.facebook.requests.MenuHeader
import com.pitchedapps.frost.facebook.requests.MenuItem
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 30/12/17.
 */
class MenuContentIItem(val data: MenuItem) :
    KauIItem<MenuContentIItem, MenuContentIItem.ViewHolder>(R.layout.iitem_menu, ::ViewHolder),
    ClickableIItemContract {

    override val url: String?
        get() = data.url

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<MenuContentIItem>(itemView) {

        val frame: ViewGroup by bindView(R.id.item_frame)
        val icon: ImageView by bindView(R.id.item_icon)
        val content: TextView by bindView(R.id.item_content)
        val badge: TextView by bindView(R.id.item_badge)

        override fun bindView(item: MenuContentIItem, payloads: MutableList<Any>) {
            frame.background = createSimpleRippleDrawable(Prefs.textColor, Prefs.nativeBgColor)
            content.setTextColor(Prefs.textColor)
            badge.setTextColor(Prefs.textColor)
            val iconUrl = item.data.pic
            if (iconUrl != null)
                GlideApp.with(itemView)
                    .load(iconUrl)
                    .transform(FrostGlide.circleCrop)
                    .into(icon.visible())
            else
                icon.gone()
            content.text = item.data.name
            badge.text = item.data.badge
        }

        override fun unbindView(item: MenuContentIItem) {
            GlideApp.with(itemView).clear(icon)
            content.text = null
            badge.text = null
        }
    }
}

class MenuHeaderIItem(val data: MenuHeader) : HeaderIItem(
    data.header,
    itemId = R.id.item_menu_header
)

class MenuFooterIItem(val data: MenuFooterItem) : TextIItem(data.name, data.url, R.id.item_menu_footer)

class MenuFooterSmallIItem(val data: MenuFooterItem) : TextIItem(data.name, data.url, R.id.item_menu_footer_small)
