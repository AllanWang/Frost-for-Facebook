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
package com.pitchedapps.frost.hilt

import android.webkit.CookieManager
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.web.state.FrostLoggerMiddleware
import com.pitchedapps.frost.web.state.FrostWebReducer
import com.pitchedapps.frost.web.state.FrostWebStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Module containing core WebView injections. */
@Module
@InstallIn(SingletonComponent::class)
object FrostWebViewModule {

  private val logger = FluentLogger.forEnclosingClass()

  @Provides @Singleton fun cookieManager(): CookieManager = CookieManager.getInstance()

  @Provides
  @Singleton
  fun frostWebStore(frostWebReducer: FrostWebReducer): FrostWebStore {
    val middleware = buildList { if (BuildConfig.DEBUG) add(FrostLoggerMiddleware()) }

    return FrostWebStore(frostWebReducer = frostWebReducer, middleware = middleware)
  }
}
