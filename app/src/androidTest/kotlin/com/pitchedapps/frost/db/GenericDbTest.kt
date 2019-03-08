package com.pitchedapps.frost.db

import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.defaultTabs
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GenericDbTest : BaseDbTest() {

    private val dao get() = db.genericDao()

    /**
     * Note that order is also preserved here
     */
    @Test
    fun save() {
        val tabs = listOf(FbItem.ACTIVITY_LOG, FbItem.BIRTHDAYS, FbItem.EVENTS, FbItem.MARKETPLACE, FbItem.ACTIVITY_LOG)
        runBlocking {
            dao.saveTabs(tabs)
            assertEquals(tabs, dao.getTabs(), "Tab saving failed")
            val newTabs = listOf(FbItem.PAGES, FbItem.MENU)
            dao.saveTabs(newTabs)
            assertEquals(newTabs, dao.getTabs(), "Tab overwrite failed")
        }
    }

    @Test
    fun defaultRetrieve() {
        runBlocking {
            assertEquals(defaultTabs(), dao.getTabs(), "Default retrieval failed")
        }
    }

    @Test
    fun ignoreErrors() {
        runBlocking {
            dao.save(GenericEntity(GenericDao.TYPE_TABS, "${FbItem.ACTIVITY_LOG.name},unknown,${FbItem.EVENTS.name}"))
            assertEquals(
                listOf(FbItem.ACTIVITY_LOG, FbItem.EVENTS),
                dao.getTabs(),
                "Tab fetching does not ignore unknown names"
            )
        }
    }
}