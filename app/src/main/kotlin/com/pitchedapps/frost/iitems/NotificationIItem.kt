package com.pitchedapps.frost.iitems

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.kau.iitems.KauIItem
import ca.allanwang.kau.ui.createSimpleRippleDrawable
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.withAlpha
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.FastAdapter
import com.pitchedapps.frost.R
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.transform
import com.pitchedapps.frost.parsers.FrostNotif
import com.pitchedapps.frost.services.FrostRunnable
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.launchWebOverlay

/**
 * Created by Allan Wang on 27/12/17.
 */
class NotificationIItem(val notification: FrostNotif, val cookie: String) : KauIItem<NotificationIItem, NotificationIItem.ViewHolder>(
        R.layout.iitem_notification, ::ViewHolder
) {

    companion object {
        fun bindEvents(adapter: FastAdapter<NotificationIItem>) {
            adapter.withSelectable(false)
                    .withOnClickListener { v, _, item, _ ->
                        val notif = item.notification
                        FrostRunnable.markNotificationRead(v.context, notif.id, item.cookie)
                        v.context.launchWebOverlay(notif.url)
                        true
                    }
        }
    }

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<NotificationIItem>(itemView) {

        val frame: ViewGroup by bindView(R.id.item_frame)
        val avatar: ImageView by bindView(R.id.item_avatar)
        val content: TextView by bindView(R.id.item_content)
        val date: TextView by bindView(R.id.item_date)

        override fun bindView(item: NotificationIItem, payloads: MutableList<Any>) {
            val notif = item.notification
            frame.background = createSimpleRippleDrawable(Prefs.textColor,
                    Prefs.bgColor.colorToForeground(if (notif.unread) 0.7f else 0.0f)
                            .withAlpha(30))
            Glide.with(itemView).load(notif.img)
                    .transform(FrostGlide.roundCorner)
                    .into(avatar)
            content.text = notif.content
            date.text = notif.time.toString()
        }

        override fun unbindView(item: NotificationIItem) {
            frame.background = null
            Glide.with(itemView).clear(avatar)
            content.text = null
            date.text = null
        }
    }
}