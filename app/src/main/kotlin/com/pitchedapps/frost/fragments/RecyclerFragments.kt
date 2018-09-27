package com.pitchedapps.frost.fragments

import com.mikepenz.fastadapter.IItem
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.requests.*
import com.pitchedapps.frost.iitems.*
import com.pitchedapps.frost.facebook.parsers.FrostNotifs
import com.pitchedapps.frost.facebook.parsers.NotifParser
import com.pitchedapps.frost.facebook.parsers.ParseResponse
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.views.FrostRecyclerView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

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

    override fun reloadImpl(progress: (Int) -> Unit, callback: (Boolean) -> Unit) {
        doAsync {
            val cookie = FbCookie.webCookie
            progress(10)
            cookie.fbRequest({ callback(false) }) {
                progress(30)
                val data = getMenuData().invoke() ?: return@fbRequest callback(false)
                if (data.data.isEmpty()) return@fbRequest callback(false)
                progress(70)
                val items = data.flatMapValid()
                progress(90)
                uiThread { adapter.add(items) }
                callback(true)
            }
        }
    }
}