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
import com.pitchedapps.frost.ext.WebTargetId
import dagger.BindsInstance
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

  fun tabId(@BindsInstance @FrostWeb tabId: WebTargetId): FrostWebComponentBuilder

  fun build(): FrostWebComponent
}

/**
 * Using this injection seems to be buggy, leading to an invalid param tabId error:
 *
 * Cause: not a valid name: tabId-4xHwVBUParam
 */
class FrostWebComposer
@Inject
internal constructor(private val frostWebComponentBuilder: FrostWebComponentBuilder) {
  fun create(tabId: WebTargetId): FrostWebCompose {
    val component = frostWebComponentBuilder.tabId(tabId).build()
    return EntryPoints.get(component, FrostWebEntryPoint::class.java).compose()
  }

  @EntryPoint
  @InstallIn(FrostWebComponent::class)
  interface FrostWebEntryPoint {
    fun compose(): FrostWebCompose
  }
}
