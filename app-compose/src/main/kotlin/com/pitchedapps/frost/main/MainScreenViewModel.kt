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
package com.pitchedapps.frost.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pitchedapps.frost.ext.GeckoContextId
import com.pitchedapps.frost.ext.idData
import com.pitchedapps.frost.ext.toContextId
import com.pitchedapps.frost.hilt.FrostComponents
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@HiltViewModel
class MainScreenViewModel
@Inject
internal constructor(
  @ApplicationContext context: Context,
  val components: FrostComponents,
) : ViewModel() {

  val contextIdFlow: Flow<GeckoContextId?> =
    components.dataStore.account.idData.map { it?.toContextId() }

  var tabIndex: Int by mutableStateOf(0)
}
