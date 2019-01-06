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
package com.pitchedapps.frost.enums

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.utils.toDrawable
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.EnumBundle
import com.pitchedapps.frost.utils.EnumBundleCompanion
import com.pitchedapps.frost.utils.EnumCompanion
import com.pitchedapps.frost.views.FrostWebView

/**
 * Created by Allan Wang on 2017-09-16.
 *
 * Options for [WebOverlayActivityBase] to give more info as to what kind of
 * overlay is present.
 *
 * For now, this is able to add new menu options upon first load
 */
enum class OverlayContext(private val menuItem: FrostMenuItem?) : EnumBundle<OverlayContext> {

    NOTIFICATION(FrostMenuItem(R.id.action_notification, FbItem.NOTIFICATIONS)),
    MESSAGE(FrostMenuItem(R.id.action_messages, FbItem.MESSAGES));

    /**
     * Inject the [menuItem] in the order that they are given at the front of the menu
     */
    fun onMenuCreate(context: Context, menu: Menu) {
        menuItem?.addToMenu(context, menu, 0)
    }

    override val bundleContract: EnumBundleCompanion<OverlayContext>
        get() = Companion

    companion object : EnumCompanion<OverlayContext>("frost_arg_overlay_context", values()) {

        /**
         * Execute selection call for an item by id
         * Returns [true] if selection was consumed, [false] otherwise
         */
        fun onOptionsItemSelected(web: FrostWebView, id: Int): Boolean {
            val item = values.firstOrNull { id == it.menuItem?.id }?.menuItem ?: return false
            web.loadUrl(item.fbItem.url, true)
            return true
        }
    }
}

/**
 * Frame for an injectable menu item
 */
class FrostMenuItem(
    val id: Int,
    val fbItem: FbItem,
    val showAsAction: Int = MenuItem.SHOW_AS_ACTION_IF_ROOM
) {
    fun addToMenu(context: Context, menu: Menu, index: Int) {
        val item = menu.add(Menu.NONE, id, index, fbItem.titleId)
        item.icon = fbItem.icon.toDrawable(context, 18)
        item.setShowAsAction(showAsAction)
    }
}
