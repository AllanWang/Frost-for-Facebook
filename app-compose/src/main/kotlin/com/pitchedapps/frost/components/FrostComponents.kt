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

import com.pitchedapps.frost.web.usecases.UseCases
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main components containing other core components.
 *
 * Modelled off of Focus:
 * https://github.com/mozilla-mobile/focus-android/blob/main/app/src/main/java/org/mozilla/focus/Components.kt
 * but with hilt
 */
@Singleton
class FrostComponents
@Inject
internal constructor(
  val core: Core,
  val useCases: UseCases,
  val dataStore: FrostDataStore,
)
