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
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.invisible
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.visible
import ca.allanwang.kau.utils.withAlpha
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter_extensions.drag.IDraggable
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 26/11/17.
 */
class TabIItem(val item: FbItem) : KauIItem<TabIItem, TabIItem.ViewHolder>(
    R.layout.iitem_tab_preview,
    { ViewHolder(it) }
), IDraggable<TabIItem, IItem<*, *>> {

    override fun withIsDraggable(draggable: Boolean): TabIItem = this

    override fun isDraggable() = true

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<TabIItem>(itemView) {

        val image: ImageView by bindView(R.id.image)
        val text: TextView by bindView(R.id.text)

        override fun bindView(item: TabIItem, payloads: MutableList<Any>) {
            val isInToolbar = adapterPosition < 4
            val color = if (isInToolbar) Prefs.iconColor else Prefs.textColor
            image.setIcon(item.item.icon, 20, color)
            if (isInToolbar)
                text.invisible()
            else {
                text.visible().setText(item.item.titleId)
                text.setTextColor(color.withAlpha(200))
            }
        }

        override fun unbindView(item: TabIItem) {
            image.setImageDrawable(null)
            text.visible().text = null
        }
    }
}
