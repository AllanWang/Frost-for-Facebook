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

import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.web.FrostAdBlock
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier annotation class Frost

@Module
@InstallIn(SingletonComponent::class)
interface FrostBindModule {
  @BindsOptionalOf @Frost fun userAgent(): String

  @BindsOptionalOf fun adBlock(): FrostAdBlock
}

/** Module containing core Frost injections. */
@Module
@InstallIn(SingletonComponent::class)
object FrostModule {

  private val logger = FluentLogger.forEnclosingClass()

  /**
   * Windows based user agent.
   *
   * Note that Facebook's mobile webpage for mobile user agents is completely different from the
   * desktop ones. All elements become divs, so nothing can be queried. There is a new UI too, but
   * it doesn't seem worth migrating all other logic over.
   */
  private const val USER_AGENT_WINDOWS =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/112.0"

  private const val USER_AGENT_WINDOWS_FROST =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.90 Safari/537.36"

  @Provides @Singleton @Frost fun userAgent(): String = USER_AGENT_WINDOWS_FROST
}
