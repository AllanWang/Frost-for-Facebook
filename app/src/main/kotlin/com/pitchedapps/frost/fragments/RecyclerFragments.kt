package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.iitems.NotificationIItem
import com.pitchedapps.frost.parsers.FrostNotifs
import com.pitchedapps.frost.parsers.NotifParser
import com.pitchedapps.frost.views.FrostRecyclerView

/**
 * Created by Allan Wang on 27/12/17.
 */
class NotificationFragment : RecyclerFragment<FrostNotifs, NotificationIItem>() {

    override val parser = NotifParser

    override fun toItems(data: FrostNotifs): List<NotificationIItem> =
            data.notifs.map(::NotificationIItem)

    override fun bind(recyclerView: FrostRecyclerView) {
        super.bind(recyclerView)
        NotificationIItem.bindEvents(adapter.fastAdapter)
    }
}