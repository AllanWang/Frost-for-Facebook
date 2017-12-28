package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.iitems.NotificationIItem
import com.pitchedapps.frost.parsers.FrostNotifs
import com.pitchedapps.frost.parsers.NotifParser
import com.pitchedapps.frost.parsers.ParseResponse
import com.pitchedapps.frost.views.FrostRecyclerView

/**
 * Created by Allan Wang on 27/12/17.
 */
class NotificationFragment : RecyclerFragment<FrostNotifs, NotificationIItem>() {

    override val parser = NotifParser

    override fun toItems(response: ParseResponse<FrostNotifs>): List<NotificationIItem> =
            response.data.notifs.map { NotificationIItem(it, response.cookie) }

    override fun bindImpl(recyclerView: FrostRecyclerView) {
        NotificationIItem.bindEvents(adapter.fastAdapter)
    }

}