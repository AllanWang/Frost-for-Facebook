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

import com.mikepenz.fastadapter.IItem
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.parsers.FrostNotifs
import com.pitchedapps.frost.facebook.parsers.NotifParser
import com.pitchedapps.frost.facebook.parsers.ParseResponse
import com.pitchedapps.frost.facebook.requests.MenuFooterItem
import com.pitchedapps.frost.facebook.requests.MenuHeader
import com.pitchedapps.frost.facebook.requests.MenuItem
import com.pitchedapps.frost.facebook.requests.MenuItemData
import com.pitchedapps.frost.facebook.requests.fbAuth
import com.pitchedapps.frost.facebook.requests.getMenuData
import com.pitchedapps.frost.iitems.ClickableIItemContract
import com.pitchedapps.frost.iitems.MenuContentIItem
import com.pitchedapps.frost.iitems.MenuFooterIItem
import com.pitchedapps.frost.iitems.MenuFooterSmallIItem
import com.pitchedapps.frost.iitems.MenuHeaderIItem
import com.pitchedapps.frost.iitems.NotificationIItem
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.views.FrostRecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Allan Wang on 27/12/17.
 */
class NotificationFragment : FrostParserFragment<FrostNotifs, NotificationIItem>() {

    override val parser = NotifParser

    override fun getDoc(cookie: String?) = frostJsoup(cookie, "${FbItem.NOTIFICATIONS.url}?more")

    override fun toItems(response: ParseResponse<FrostNotifs>): List<NotificationIItem> =
        response.data.notifs.map { NotificationIItem(it, response.cookie) }

    override fun bindImpl(recyclerView: FrostRecyclerView) {
        NotificationIItem.bindEvents(adapter)
    }
}

class MenuFragment : GenericRecyclerFragment<MenuItemData, IItem<*, *>>() {

    override fun mapper(data: MenuItemData): IItem<*, *> = when (data) {
        is MenuHeader -> MenuHeaderIItem(data)
        is MenuItem -> MenuContentIItem(data)
        is MenuFooterItem ->
            if (data.isSmall) MenuFooterSmallIItem(data)
            else MenuFooterIItem(data)
        else -> throw IllegalArgumentException("Menu item in fragment has invalid type ${data::class.java.simpleName}")
    }

    override fun bindImpl(recyclerView: FrostRecyclerView) {
        ClickableIItemContract.bindEvents(adapter)
    }

    override suspend fun reloadImpl(progress: (Int) -> Unit): List<MenuItemData>? = withContext(Dispatchers.IO) {
        val cookie = FbCookie.webCookie ?: return@withContext null
        progress(10)
        val auth = fbAuth.fetch(cookie).await()
        progress(30)
        val data = auth.getMenuData().invoke() ?: return@withContext null
        if (data.data.isEmpty()) return@withContext null
        progress(70)
        val items = data.flatMapValid()
        progress(90)
        return@withContext items
    }
}
