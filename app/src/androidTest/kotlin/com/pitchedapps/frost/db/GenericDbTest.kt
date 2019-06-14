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
            dao._save(GenericEntity(GenericDao.TYPE_TABS, "${FbItem.ACTIVITY_LOG.name},unknown,${FbItem.EVENTS.name}"))
            assertEquals(
                listOf(FbItem.ACTIVITY_LOG, FbItem.EVENTS),
                dao.getTabs(),
                "Tab fetching does not ignore unknown names"
            )
        }
    }
}
