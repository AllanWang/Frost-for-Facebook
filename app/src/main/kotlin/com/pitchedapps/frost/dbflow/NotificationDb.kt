package com.pitchedapps.frost.dbflow

import com.pitchedapps.frost.utils.L
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Database(name = NotificationDb.NAME, version = NotificationDb.VERSION)
object NotificationDb {
    const val NAME = "Notifications"
    const val VERSION = 1
}

@Table(database = NotificationDb::class, allFields = true, primaryKeyConflict = ConflictAction.REPLACE)
data class NotificationModel(@PrimaryKey var id: Long = -1L, var epoch: Long = -1L) : BaseModel()

fun lastNotificationTime(id: Long): Long = (select from NotificationModel::class where (NotificationModel_Table.id eq id)).querySingle()?.epoch ?: -1L

fun saveNotificationTime(notificationModel: NotificationModel, callback: (() -> Unit)? = null) {
    notificationModel.async save {
        L.d("Fb notification model saved", notificationModel.toString())
        callback?.invoke()
    }
}