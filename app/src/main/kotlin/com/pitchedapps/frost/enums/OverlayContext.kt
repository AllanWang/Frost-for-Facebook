package com.pitchedapps.frost.enums

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.kau.utils.toDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem

/**
 * Created by Allan Wang on 2017-09-16.
 *
 * Options for [WebOverlayActivityBase] to give more info as to what kind of
 * overlay is present.
 *
 * For now, this is able to add new menu options open first load
 */
enum class OverlayContext(private val menuItem: FrostMenuItem?) {
    DEFAULT(null),
    NOTIFICATION(FrostMenuItem(R.id.action_notification, FbItem.NOTIFICATIONS.icon, R.string.notifications) { context ->
    }),
    MESSAGE(FrostMenuItem(R.id.action_messages, FbItem.MESSAGES.icon, R.string.messages) { context ->
    });

    /**
     * Inject the [menuItem] in the order that they are given at the front of the menu
     */
    fun onMenuCreate(context: Context, menu: Menu) {
        menuItem?.addToMenu(context, menu, 0)
    }

    companion object {

        val values = OverlayContext.values() //save one instance
        /**
         * Execute selection call for an item by id
         * Returns [true] if selection was consumed, [false] otherwise
         */
        fun onOptionsItemSelected(context: Context, id: Int): Boolean {
            val consumer = values.firstOrNull { id == it.menuItem?.id } ?: return false
            consumer.menuItem!!.onClick(context)
            return true
        }
    }
}

/**
 * Frame for an injectable menu item
 */
class FrostMenuItem(
        val id: Int,
        val iicon: IIcon,
        val stringRes: Int,
        val showAsAction: Int = MenuItem.SHOW_AS_ACTION_ALWAYS,
        val onClick: (context: Context) -> Unit) {
    fun addToMenu(context: Context, menu: Menu, index: Int) {
        val item = menu.add(Menu.NONE, id, index, stringRes)
        item.icon = iicon.toDrawable(context, 18)
        item.setShowAsAction(showAsAction)
    }
}