package com.pitchedapps.frost

import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration

/**
 * Created by Allan Wang on 2017-05-30.
 */

/**
 * Description:
 */
@Database(version = TestDatabase.VERSION, name = TestDatabase.NAME)
object TestDatabase {

    const val VERSION = 1

    const val NAME = "TestDatabase"

}