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
import ca.allanwang.kau.utils.withAlpha
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.commons.utils.DiffCallback
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.parsers.FrostNotif
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.isIndependent
import com.pitchedapps.frost.utils.launchWebOverlay

/**
 * Created by Allan Wang on 27/12/17.
 */
class NotificationIItem(val notification: FrostNotif, val cookie: String) :
    KauIItem<NotificationIItem, NotificationIItem.ViewHolder>(
        R.layout.iitem_notification, ::ViewHolder
    ) {

    companion object {
        fun bindEvents(adapter: ItemAdapter<NotificationIItem>) {
            adapter.fastAdapter.withSelectable(false)
                .withOnClickListener { v, _, item, position ->
                    val notif = item.notification
                    if (notif.unread) {
                        adapter.set(
                            position,
                            NotificationIItem(notif.copy(unread = false), item.cookie)
                        )
                    }
                    // TODO temp fix. If url is dependent, we cannot load it directly
                    v!!.context.launchWebOverlay(if (notif.url.isIndependent) notif.url else FbItem.NOTIFICATIONS.url)
                    true
                }
        }

        //todo see if necessary
        val DIFF: DiffCallback<NotificationIItem> by lazy(::Diff)
    }

    private class Diff : DiffCallback<NotificationIItem> {

        override fun areItemsTheSame(oldItem: NotificationIItem, newItem: NotificationIItem) =
            oldItem.notification.id == newItem.notification.id

        override fun areContentsTheSame(oldItem: NotificationIItem, newItem: NotificationIItem) =
            oldItem.notification == newItem.notification

        override fun getChangePayload(
            oldItem: NotificationIItem,
            oldItemPosition: Int,
            newItem: NotificationIItem,
            newItemPosition: Int
        ): Any? {
            return newItem
        }
    }

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<NotificationIItem>(itemView) {

        private val frame: ViewGroup by bindView(R.id.item_frame)
        private val avatar: ImageView by bindView(R.id.item_avatar)
        private val content: TextView by bindView(R.id.item_content)
        private val date: TextView by bindView(R.id.item_date)
        private val thumbnail: ImageView by bindView(R.id.item_thumbnail)

        private val glide
            get() = GlideApp.with(itemView)

        override fun bindView(item: NotificationIItem, payloads: MutableList<Any>) {
            val notif = item.notification
            frame.background = createSimpleRippleDrawable(
                Prefs.textColor,
                Prefs.nativeBgColor(notif.unread)
            )
            content.setTextColor(Prefs.textColor)
            date.setTextColor(Prefs.textColor.withAlpha(150))

            val glide = glide
            glide.load(notif.img)
                .transform(FrostGlide.circleCrop)
                .into(avatar)
            if (notif.thumbnailUrl != null)
                glide.load(notif.thumbnailUrl).into(thumbnail.visible())

            content.text = notif.content
            date.text = notif.timeString
        }

        override fun unbindView(item: NotificationIItem) {
            frame.background = null
            val glide = glide
            glide.clear(avatar)
            glide.clear(thumbnail)
            thumbnail.gone()
            content.text = null
            date.text = null
        }
    }
}
