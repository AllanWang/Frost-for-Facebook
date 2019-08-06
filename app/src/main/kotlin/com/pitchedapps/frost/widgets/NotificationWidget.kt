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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import ca.allanwang.kau.utils.dimenPixelSize
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.db.NotificationDao
import com.pitchedapps.frost.db.selectNotificationsSync
import com.pitchedapps.frost.glide.FrostGlide
import com.pitchedapps.frost.glide.GlideApp
import com.pitchedapps.frost.services.NotificationContent
import com.pitchedapps.frost.services.NotificationType
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.toReadableTime
import org.koin.core.KoinComponent
import org.koin.core.inject

class NotificationWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val type = NotificationType.GENERAL
        val userId = Prefs.userId
        val intent = NotificationWidgetService.createIntent(context, type, userId)
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_notifications)

            views.setBackgroundColor(R.id.widget_layout_toolbar, Prefs.headerColor)
            views.setIcon(R.id.img_frost, context, R.drawable.frost_f_24, Prefs.iconColor)
            views.setOnClickPendingIntent(
                R.id.img_frost,
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
            )

            views.setBackgroundColor(R.id.widget_notification_list, Prefs.bgColor)
            views.setRemoteAdapter(R.id.widget_notification_list, intent)

            val pendingIntentTemplate = PendingIntent.getActivity(
                context,
                0,
                type.createCommonIntent(context, userId),
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            views.setPendingIntentTemplate(R.id.widget_notification_list, pendingIntentTemplate)

            appWidgetManager.updateAppWidget(id, views)
        }
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_notification_list)
    }

    companion object {
        fun forceUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids =
                manager.getAppWidgetIds(ComponentName(context, NotificationWidget::class.java))
            val intent = Intent().apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}

private const val NOTIF_WIDGET_TYPE = "notif_widget_type"
private const val NOTIF_WIDGET_USER_ID = "notif_widget_user_id"

private fun RemoteViews.setBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int) {
    setInt(viewId, "setBackgroundColor", color)
}

/**
 * Adds backward compatibility to setting tinted icons
 */
private fun RemoteViews.setIcon(@IdRes viewId: Int, context: Context, @DrawableRes res: Int, @ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val icon =
            Icon.createWithResource(context, res).setTint(color).setTintMode(PorterDuff.Mode.SRC_IN)
        setImageViewIcon(viewId, icon)
    } else {
        val bitmap = BitmapFactory.decodeResource(context.resources, res)
        if (bitmap != null) {
            val paint = Paint()
            paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            setImageViewBitmap(viewId, result)
        } else {
            // Fallback to just icon
            setImageViewResource(viewId, res)
        }
    }
}

class NotificationWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        NotificationWidgetDataProvider(this, intent)

    companion object {
        fun createIntent(context: Context, type: NotificationType, userId: Long): Intent =
            Intent(context, NotificationWidgetService::class.java)
                .putExtra(NOTIF_WIDGET_TYPE, type.name)
                .putExtra(NOTIF_WIDGET_USER_ID, userId)
    }
}

class NotificationWidgetDataProvider(val context: Context, val intent: Intent) :
    RemoteViewsService.RemoteViewsFactory,
    KoinComponent {

    private val notifDao: NotificationDao by inject()
    @Volatile
    private var content: List<NotificationContent> = emptyList()

    private val type = NotificationType.valueOf(intent.getStringExtra(NOTIF_WIDGET_TYPE))

    private val userId = intent.getLongExtra(NOTIF_WIDGET_USER_ID, -1)

    private val avatarSize = context.dimenPixelSize(R.dimen.avatar_image_size)

    private val glide = GlideApp.with(context).asBitmap()

    private fun loadNotifications() {
        content = notifDao.selectNotificationsSync(userId, type.channelId)
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
        views.setBackgroundColor(R.id.item_frame, Prefs.nativeBgColor(notif.unread))
        views.setTextColor(R.id.item_content, Prefs.textColor)
        views.setTextViewText(R.id.item_content, notif.text)
        views.setTextColor(R.id.item_date, Prefs.textColor.withAlpha(150))
        views.setTextViewText(R.id.item_date, notif.timestamp.toReadableTime(context))

        val avatar = glide.load(notif.profileUrl).transform(FrostGlide.circleCrop)
            .submit(avatarSize, avatarSize).get()
        views.setImageViewBitmap(R.id.item_avatar, avatar)
        views.setOnClickFillInIntent(R.id.item_frame, type.putContentExtra(Intent(), notif))
        return views
    }

    override fun getCount(): Int = content.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
    }
}
