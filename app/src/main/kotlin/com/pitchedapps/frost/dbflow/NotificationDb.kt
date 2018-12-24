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
package com.pitchedapps.frost.dbflow

import com.pitchedapps.frost.utils.L
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.async
import com.raizlabs.android.dbflow.kotlinextensions.eq
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Created by Allan Wang on 2017-05-30.
 */

@Database(version = NotificationDb.VERSION)
object NotificationDb {
    const val NAME = "Notifications"
    const val VERSION = 2
}

@Migration(version = 2, database = NotificationDb::class)
class NotificationMigration2(modelClass: Class<NotificationModel>) :
    AlterTableMigration<NotificationModel>(modelClass) {
    override fun onPreMigrate() {
        super.onPreMigrate()
        addColumn(SQLiteType.INTEGER, "epochIm")
        L.d { "Added column" }
    }
}

@Table(database = NotificationDb::class, allFields = true, primaryKeyConflict = ConflictAction.REPLACE)
data class NotificationModel(
    @PrimaryKey var id: Long = -1L,
    var epoch: Long = -1L,
    var epochIm: Long = -1L
) : BaseModel()

fun lastNotificationTime(id: Long): NotificationModel =
    (select from NotificationModel::class where (NotificationModel_Table.id eq id)).querySingle()
        ?: NotificationModel(id = id)

fun saveNotificationTime(notificationModel: NotificationModel, callback: (() -> Unit)? = null) {
    notificationModel.async save {
        L.d { "Fb notification model saved" }
        L._d { notificationModel }
        callback?.invoke()
    }
}
