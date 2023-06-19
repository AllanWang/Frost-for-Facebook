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
package com.pitchedapps.frost

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.hilt.FrostComponents
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Provider

/** Frost Application. */
@HiltAndroidApp
class FrostApp : Application() {

  @Inject lateinit var componentsProvider: Provider<FrostComponents>

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      registerActivityLifecycleCallbacks(
        object : ActivityLifecycleCallbacks {
          override fun onActivityPaused(activity: Activity) {}

          override fun onActivityResumed(activity: Activity) {}

          override fun onActivityStarted(activity: Activity) {}

          override fun onActivityDestroyed(activity: Activity) {
            logger.atFine().log("Activity %s destroyed", activity.localClassName)
          }

          override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

          override fun onActivityStopped(activity: Activity) {}

          override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            logger.atFine().log("Activity %s created", activity.localClassName)
          }
        },
      )
    }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
