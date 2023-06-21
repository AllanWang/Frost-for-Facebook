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
package com.pitchedapps.frost.compose.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State for tracking shaking animation
 *
 * Note: Used some other material states as reference. This however will recompose the function
 * holding the state during shakes.
 */
@Composable
fun rememberShakeState(): ShakeState {
  val scope = rememberCoroutineScope()
  val state = remember(scope) { ShakeState(scope) }

  return state
}

class ShakeState
internal constructor(
  private val animationScope: CoroutineScope,
) {

  private val rotationAnimatable = Animatable(0f)

  internal val rotation
    get() = rotationAnimatable.value

  fun shake() {
    animationScope.launch {
      rotationAnimatable.stop()
      rotationAnimatable.animateTo(
        0f,
        initialVelocity = 200f,
        animationSpec =
          spring(
            dampingRatio = 0.3f,
            stiffness = 200f,
          ),
      )
    }
  }
}

fun Modifier.shake(state: ShakeState, enabled: Boolean = true) =
  inspectable(
    inspectorInfo =
      debugInspectorInfo {
        name = "shake"
        properties["enabled"] = enabled
      },
  ) {
    Modifier.rotate(state.rotation)
  }
