package com.pitchedapps.frost.db

import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.defaultTabs
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationDbTest : BaseDbTest() {

    private val dao get() = db.notifDao()

    /**
     * Note that order is also preserved here
     */
    @Test
    fun save() {

    }
}