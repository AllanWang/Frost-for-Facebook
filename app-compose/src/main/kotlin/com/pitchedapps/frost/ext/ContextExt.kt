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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

inline fun <reified T : Activity> Context.launchActivity(
  clearStack: Boolean = false,
  bundleBuilder: Bundle.() -> Unit = {},
  intentBuilder: Intent.() -> Unit = {}
) {
  val intent = Intent(this, T::class.java)
  if (clearStack) {
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  intent.intentBuilder()
  val bundle = Bundle()
  bundle.bundleBuilder()
  startActivity(intent, bundle.takeIf { !it.isEmpty })
  if (clearStack && this is Activity) {
    finish()
  }
}
