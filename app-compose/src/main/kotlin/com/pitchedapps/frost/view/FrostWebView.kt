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
package com.pitchedapps.frost.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.pitchedapps.frost.hilt.Frost
import dagger.hilt.android.AndroidEntryPoint
import java.util.Optional
import javax.inject.Inject

@AndroidEntryPoint
class FrostWebView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  NestedWebView(context, attrs, defStyleAttr) {

  @Inject @Frost lateinit var userAgent: Optional<String>

  init {
    userAgent.ifPresent {
      settings.userAgentString = it
      println("Set user agent to $it")
    }
    with(settings) {
      // noinspection SetJavaScriptEnabled
      javaScriptEnabled = true
      mediaPlaybackRequiresUserGesture = false // TODO check if we need this
      allowFileAccess = true
      // textZoom
      domStorageEnabled = true

      setLayerType(LAYER_TYPE_HARDWARE, null)
      setBackgroundColor(Color.TRANSPARENT)
      // Download listener
      // JS Interface
    }
  }
}
