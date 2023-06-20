/*
 * Copyright 2021 Allan Wang
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
package com.pitchedapps.frost.webview

import com.pitchedapps.frost.compose.webview.FrostWebCompose
import com.pitchedapps.frost.web.state.FrostLoggerMiddleware
import com.pitchedapps.frost.web.state.FrostWebStore
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.DefineComponent
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Scope

/**
 * Defines a new scope for Frost web related content.
 *
 * This is a subset of [dagger.hilt.android.scopes.ViewScoped]
 */
@Scope
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class FrostWebScoped

@Qualifier annotation class FrostWeb

@FrostWebScoped @DefineComponent(parent = ViewModelComponent::class) interface FrostWebComponent

@DefineComponent.Builder
interface FrostWebComponentBuilder {
  fun id(@BindsInstance @FrostWeb id: String): FrostWebComponentBuilder

  fun build(): FrostWebComponent
}

@Module
@InstallIn(FrostWebComponent::class)
internal object FrostWebModule {

  @Provides
  @FrostWebScoped
  fun frostStore(@FrostWeb id: String): FrostWebStore {
    val logger = FrostLoggerMiddleware(id)

    return FrostWebStore(tag = id, middleware = listOf(logger))
  }
}

class FrostWebComposer
@Inject
internal constructor(private val frostWebComponentBuilder: FrostWebComponentBuilder) {
  fun create(id: String): FrostWebCompose {
    val frostWebComponent = frostWebComponentBuilder.id(id).build()
    val frostWebEntryPoint = EntryPoints.get(frostWebComponent, FrostWebEntryPoint::class.java)
    return frostWebEntryPoint.frostWebCompose()
  }

  @EntryPoint
  @InstallIn(FrostWebComponent::class)
  interface FrostWebEntryPoint {
    fun frostWebCompose(): FrostWebCompose
  }
}
