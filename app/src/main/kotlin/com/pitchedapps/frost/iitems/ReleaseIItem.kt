package com.pitchedapps.frost.iitems

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.utils.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter_extensions.drag.IDraggable
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.services.FrostRelease
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 26/11/17.
 */
class ReleaseIItem(val item: FrostRelease) : KauIItem<ReleaseIItem, ReleaseIItem.ViewHolder>(
        R.layout.iitem_tab_preview,
        { ViewHolder(it) }
) {

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<ReleaseIItem>(itemView) {

        val image: ImageView by bindView(R.id.image)
        val text: TextView by bindView(R.id.text)

        override fun bindView(item: ReleaseIItem, payloads: MutableList<Any>) {

        }

        override fun unbindView(item: ReleaseIItem) {

        }

    }
}