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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

@Composable
fun Modifier.thenIf(condition: Boolean, action: @Composable () -> Modifier): Modifier =
  if (condition) then(action()) else this

fun Offset.toIntOffset() = IntOffset(x.roundToInt(), y.roundToInt())

fun IntSize.toDpSize(density: Density): DpSize {
  return with(density) { DpSize(width.toDp(), height.toDp()) }
}

/**
 * Helper for functions that take in nullable compose lambdas.
 *
 * If the input is null, return null. Otherwise, return the provided composable lambda.
 */
fun <T> T?.optionalCompose(action: @Composable (T) -> Unit): (@Composable () -> Unit)? {
  if (this == null) return null
  return { action(this) }
}
