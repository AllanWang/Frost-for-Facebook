/*
 * Copyright 2023 Allan Wang
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
package com.pitchedapps.frost

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.components.FrostDataStore
import com.pitchedapps.frost.db.FrostDb
import com.pitchedapps.frost.ext.FrostAccountId
import com.pitchedapps.frost.ext.idData
import com.pitchedapps.frost.ext.launchActivity
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.main.MainActivity
import com.pitchedapps.frost.web.state.FrostWebStore
import com.pitchedapps.frost.web.usecases.UseCases
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Start activity.
 *
 * This is the launcher activity, and should not be moved/renamed. The activity itself is transient,
 * and will launch another activity without history after doing initialization work.
 */
@AndroidEntryPoint
class StartActivity : ComponentActivity() {

  @Inject lateinit var frostDb: FrostDb

  @Inject lateinit var dataStore: FrostDataStore

  @Inject lateinit var store: FrostWebStore
  @Inject lateinit var useCases: UseCases

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      val id = withContext(Dispatchers.IO) { getCurrentAccountId() }

      logger.atInfo().log("Starting Frost with id %s", id)

      // TODO load real tabs
      useCases.homeTabs.setHomeTabs(listOf(FbItem.Feed, FbItem.Menu))

      launchActivity<MainActivity>(
        intentBuilder = {
          flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
              Intent.FLAG_ACTIVITY_CLEAR_TOP or
              Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
      )
    }
  }

  private suspend fun getCurrentAccountId(): FrostAccountId {
    val currentId = dataStore.account.idData.firstOrNull()
    if (currentId != null) return currentId

    val newId = getAnyAccountId()
    dataStore.account.updateData { it.toBuilder().setAccountId(newId).build() }
    return FrostAccountId(newId)
  }

  private fun getAnyAccountId(): Long {
    val account = frostDb.accountsQueries.selectAll().executeAsOneOrNull()
    if (account != null) return account.id
    frostDb.accountsQueries.insertNew()
    return frostDb.accountsQueries.selectAll().executeAsOne().id
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
