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
package com.pitchedapps.frost.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@RunWith(AndroidJUnit4::class)
abstract class BaseDbTest {

    protected lateinit var db: FrostDatabase

    @BeforeTest
    fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val privateDb = Room.inMemoryDatabaseBuilder(
            context, FrostPrivateDatabase::class.java
        ).build()
        val publicDb = Room.inMemoryDatabaseBuilder(
            context, FrostPublicDatabase::class.java
        ).build()
        db = FrostDatabase(privateDb, publicDb)
    }

    @AfterTest
    fun after() {
        db.close()
    }
}
