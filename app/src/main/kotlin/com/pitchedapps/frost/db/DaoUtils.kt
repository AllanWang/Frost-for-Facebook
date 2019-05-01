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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps dao calls to work with coroutines
 * Non transactional queries were supposed to be fixed in https://issuetracker.google.com/issues/69474692,
 * but it still requires dispatch from a non ui thread.
 * This avoids that constraint
 */
suspend inline fun <T> dao(crossinline block: () -> T) = withContext(Dispatchers.IO) { block() }
