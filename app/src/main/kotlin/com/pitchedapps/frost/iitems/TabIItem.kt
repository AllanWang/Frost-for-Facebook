package com.pitchedapps.frost.iitems

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.kotlin.lazyContext
import ca.allanwang.kau.utils.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter_extensions.drag.IDraggable
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by user7681 on 26/11/17.
 */
class TabIItem(val item: FbItem, var isPreview: Boolean) : KauIItem<TabIItem, TabIItem.ViewHolder>(
        R.layout.iitem_tab_preview,
        { ViewHolder(it) }
), IDraggable<TabIItem, IItem<*, *>> {

    override fun withIsDraggable(draggable: Boolean): TabIItem {
        this.isPreview = draggable
        return this
    }

    fun asPreview() = withIsDraggable(true)

    fun asOption() = withIsDraggable(false)

    override fun isDraggable() = isPreview

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<TabIItem>(itemView) {

        val image: ImageView by bindView(R.id.image)
        val text: TextView by bindView(R.id.text)

        override fun bindView(item: TabIItem, payloads: MutableList<Any>?) {
            val color = if (item.isPreview) Prefs.iconColor else Prefs.textColor
            image.setIcon(item.item.icon, 20, color)
            if (item.isPreview)
                text.gone()
            else {
                text.setText(item.item.titleId)
                text.setTextColor(color.withAlpha(200))
            }
        }

        override fun unbindView(item: TabIItem) {
            image.setImageDrawable(null)
            text.visible().text = null
        }

    }
}