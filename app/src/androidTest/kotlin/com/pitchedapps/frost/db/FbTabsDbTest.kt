package com.pitchedapps.frost.db

import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.defaultTabs
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class FbTabsDbTest : BaseDbTest() {
    
    private val dao get() = db.tabDao()

    /**
     * Note that order is also preserved here
     */
    @Test
    fun save() {
        val tabs = listOf(FbItem.ACTIVITY_LOG, FbItem.BIRTHDAYS, FbItem.EVENTS, FbItem.MARKETPLACE, FbItem.ACTIVITY_LOG)
        runBlocking {
            dao.save(tabs)
            assertEquals(tabs, dao.selectAll(), "Tab saving failed")
            val newTabs = listOf(FbItem.PAGES, FbItem.MENU)
            dao.save(newTabs)
            assertEquals(newTabs, dao.selectAll(), "Tab saving does not delete preexisting items")
        }
    }

    @Test
    fun defaultRetrieve() {
        runBlocking {
            assertEquals(defaultTabs(), dao.selectAll(), "Default retrieval failed")
        }
    }
}