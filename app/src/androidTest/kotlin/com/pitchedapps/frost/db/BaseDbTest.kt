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