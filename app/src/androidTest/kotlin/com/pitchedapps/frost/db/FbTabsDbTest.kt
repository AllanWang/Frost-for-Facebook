package com.pitchedapps.frost.db

import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.defaultTabs
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class FbTabsDbTest : BaseDbTest() {

    /**
     * Note that order is also preserved here
     */
    @Test
    fun save() {
        val tabs = listOf(FbItem.ACTIVITY_LOG, FbItem.BIRTHDAYS, FbItem.EVENTS, FbItem.MARKETPLACE, FbItem.ACTIVITY_LOG)
        runBlocking {
            db.tabDao().save(tabs)
            assertEquals(tabs, db.tabDao().selectAll(), "Tab saving failed")
            val newTabs = listOf(FbItem.PAGES, FbItem.MENU)
            db.tabDao().save(newTabs)
            assertEquals(newTabs, db.tabDao().selectAll(), "Tab saving does not delete preexisting items")
        }
    }

    @Test
    fun defaultRetrieve() {
        runBlocking {
            assertEquals(defaultTabs(), db.tabDao().selectAll(), "Default retrieval failed")
        }
    }
}