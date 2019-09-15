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
package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.parsers.FrostNotifs
import com.pitchedapps.frost.facebook.parsers.NotifParser
import com.pitchedapps.frost.facebook.parsers.ParseResponse
import com.pitchedapps.frost.iitems.NotificationIItem
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.views.FrostRecyclerView

/**
 * Created by Allan Wang on 27/12/17.
 *
 * Retained as an example. Deletion made at https://github.com/AllanWang/Frost-for-Facebook/pull/1542
 */
@Deprecated(message = "Retained as an example; currently does not support marking a notification as read")
class NotificationFragment : FrostParserFragment<FrostNotifs, NotificationIItem>() {

    override val parser = NotifParser

    override fun getDoc(cookie: String?) = frostJsoup(cookie, "${FbItem.NOTIFICATIONS.url}?more")

    override fun toItems(response: ParseResponse<FrostNotifs>): List<NotificationIItem> =
        response.data.notifs.map { NotificationIItem(it, response.cookie) }

    override fun bindImpl(recyclerView: FrostRecyclerView) {
        NotificationIItem.bindEvents(adapter)
    }
}
