package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.requests.*
import com.pitchedapps.frost.iitems.MenuContentIItem
import com.pitchedapps.frost.iitems.MenuHeaderIItem
import com.pitchedapps.frost.iitems.MenuIItem
import com.pitchedapps.frost.iitems.NotificationIItem
import com.pitchedapps.frost.parsers.FrostNotifs
import com.pitchedapps.frost.parsers.NotifParser
import com.pitchedapps.frost.parsers.ParseResponse
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.views.FrostRecyclerView
import org.jetbrains.anko.doAsync

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

class MenuFragment : GenericRecyclerFragment<MenuItemData, MenuIItem<*, *>>() {

    override fun mapper(data: MenuItemData) = when (data) {
        is MenuHeader -> MenuHeaderIItem(data)
        is MenuItem -> MenuContentIItem(data)
        else -> throw IllegalArgumentException("Menu item in fragment has invalid type ${data::class.java.simpleName}")
    }

    override fun bindImpl(recyclerView: FrostRecyclerView) {
        MenuIItem.bindEvents(adapter)
    }

    override fun reload(progress: (Int) -> Unit, callback: (Boolean) -> Unit) {
        doAsync {
            val cookie = FbCookie.webCookie
            progress(10)
            cookie.fbRequest({ callback(false) }) {
                progress(30)
                val data = getMenuData().invoke() ?: return@fbRequest callback(false)
                if (data.data.isEmpty()) return@fbRequest callback(false)
                progress(60)
                val items = mutableListOf<MenuItemData>()
                data.data.forEach {
                    items.add(it)
                    it.all.forEach {
                        items.add(it)
                    }
                }
                progress(80)
                adapter.add(items)
                callback(true)
            }
        }
    }
}