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
package com.pitchedapps.frost.components

import androidx.datastore.core.DataStore
import com.pitchedapps.frost.proto.Account
import com.pitchedapps.frost.proto.settings.Appearance
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FrostDataStore
@Inject
internal constructor(
  private val accountProvider: Provider<DataStore<Account>>,
  private val appearanceProvider: Provider<DataStore<Appearance>>,
) {
  val account: DataStore<Account>
    get() = accountProvider.get()

  val appearance: DataStore<Appearance>
    get() = appearanceProvider.get()
}
