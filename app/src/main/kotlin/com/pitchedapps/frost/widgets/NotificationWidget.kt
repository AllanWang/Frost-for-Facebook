/*
 * Copyright 2019 Allan Wang
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
package com.pitchedapps.frost.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.ColorInt
import ca.allanwang.kau.utils.dimenPixelSize
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.db.NotificationDao
import com.pitchedapps.frost.db.selectNotificationsSync
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.services.NOTIF_CHANNEL_GENERAL
import com.pitchedapps.frost.services.NotificationContent
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.widgets.NotificationWidget.Companion.NOTIF_WIDGET_IDS
import com.pitchedapps.frost.widgets.NotificationWidget.Companion.NOTIF_WIDGET_TYPE
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class NotificationWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val intent = NotificationWidgetService.createIntent(context, NOTIF_CHANNEL_GENERAL, appWidgetIds)
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_notifications)
            views.setBackgroundColor(R.id.widget_layout_container, Prefs.bgColor)
            views.setRemoteAdapter(R.id.widget_notification_list, intent)
            appWidgetManager.updateAppWidget(id, views)
        }
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_notification_list)
    }

    companion object {
        const val NOTIF_WIDGET_TYPE = "notif_widget_type"
        const val NOTIF_WIDGET_IDS = "notif_widget_ids"
    }
}

private fun RemoteViews.setBackgroundColor(viewId: Int, @ColorInt color: Int) {
    setInt(viewId, "setBackgroundColor", color)
}

class NotificationWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = NotificationWidgetDataProvider(this, intent)

    companion object {
        fun createIntent(context: Context, type: String, appWidgetIds: IntArray): Intent =
            Intent(context, NotificationWidgetService::class.java)
                .putExtra(NOTIF_WIDGET_TYPE, type)
                .putExtra(NOTIF_WIDGET_IDS, appWidgetIds)
    }
}

class NotificationWidgetDataProvider(val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory,
    KoinComponent {

    private val notifDao: NotificationDao by inject()
    @Volatile
    private var content: List<NotificationContent> = emptyList()

    private val type = intent.getStringExtra(NOTIF_WIDGET_TYPE)

    private val widgetIds = intent.getIntArrayExtra(NOTIF_WIDGET_IDS)

    private val avatarSize = context.dimenPixelSize(R.dimen.avatar_image_size)

    private val glide = GlideApp.with(context).asBitmap()

    private fun loadNotifications() {
        content = notifDao.selectNotificationsSync(Prefs.userId, type)
        L._d { "Updated notif widget with ${content.size} items" }
    }

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        loadNotifications()
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long = content[position].id

    override fun hasStableIds(): Boolean = true

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_notification_item)
        val notif = content[position]
        L._d { "View $position $notif" }
        views.setBackgroundColor(R.id.item_frame, Prefs.nativeBgColor(notif.unread))
        views.setTextColor(R.id.item_content, Prefs.textColor)
        views.setTextViewText(R.id.item_content, notif.text)
        views.setTextColor(R.id.item_date, Prefs.textColor.withAlpha(150))
        views.setTextViewText(R.id.item_date, notif.timestamp.toString()) // TODO
//        views.setOnClickPendingIntent()
        val avatar = glide.load(notif.profileUrl).transform(FrostGlide.circleCrop).submit(avatarSize, avatarSize).get()
        views.setImageViewBitmap(R.id.item_avatar, avatar)
        return views
    }

    override fun getCount(): Int = content.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
    }
}