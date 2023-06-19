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
package com.pitchedapps.frost.ext

import androidx.datastore.core.DataStore
import com.pitchedapps.frost.proto.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Representation of unique frost account id.
 *
 * Account ids are identifiers specific to Frost, and group ids/info from other sites.
 */
@JvmInline value class FrostAccountId(val id: Long)

/**
 * Representation of gecko context id.
 *
 * Id is used to split cookies between accounts. [GeckoContextId] must be fixed per
 * [FrostAccountId].
 */
@JvmInline value class GeckoContextId(val id: String)

/**
 * Helper to get [FrostAccountId] from account data store.
 *
 * If account id is not initialized, returns null.
 */
val DataStore<Account>.idData: Flow<FrostAccountId?>
  get() = data.map { if (it.hasAccountId()) FrostAccountId(it.accountId) else null }

/**
 * Convert accountId to contextId.
 *
 * Note that contextId cannot be modified, as it is linked to all cookie info. Account ids start at
 * 1, and it is important not to allow a default [GeckoContextId] with id 0. Doing so would be a
 * bug, and may cause users to mix logins from multiple Frost accounts.
 */
fun FrostAccountId.toContextId(): GeckoContextId {
  require(id > 0L) { "Invalid accountId $id" }
  return GeckoContextId(id = "frost-context-$id")
}
