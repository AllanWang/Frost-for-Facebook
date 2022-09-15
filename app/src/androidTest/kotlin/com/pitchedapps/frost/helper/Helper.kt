/*
 * Copyright 2018 Allan Wang
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
package com.pitchedapps.frost.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream

val context: Context
  get() = InstrumentationRegistry.getInstrumentation().targetContext

fun getAsset(asset: String): InputStream = context.assets.open(asset)

private class Helper

fun getResource(resource: String): InputStream =
  Helper::class.java.classLoader!!.getResource(resource).openStream()

inline fun <reified A : Activity> activityRule(
  intentAction: Intent.() -> Unit = {},
  activityOptions: Bundle? = null
): ActivityScenarioRule<A> {
  val intent = Intent(ApplicationProvider.getApplicationContext(), A::class.java).also(intentAction)
  return ActivityScenarioRule(intent, activityOptions)
}

const val TEST_FORMATTED_URL = "https://www.google.com"
