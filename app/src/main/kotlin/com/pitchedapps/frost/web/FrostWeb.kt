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
package com.pitchedapps.frost.web

import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.views.FrostWebView
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.DefineComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Qualifier
import javax.inject.Scope

/**
 * Defines a new scope for Frost web related content.
 *
 * This is a subset of [dagger.hilt.android.scopes.ViewScoped]
 */
@Scope
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE,
    AnnotationTarget.CLASS
)
annotation class FrostWebScoped

@FrostWebScoped
@DefineComponent(parent = ViewComponent::class)
interface FrostWebComponent

@DefineComponent.Builder
interface FrostWebComponentBuilder {
    fun frostParent(@BindsInstance parent: FrostContentParent): FrostWebComponentBuilder
    fun frostWebView(@BindsInstance web: FrostWebView): FrostWebComponentBuilder
    fun build(): FrostWebComponent
}

@EntryPoint
@InstallIn(FrostWebComponent::class)
interface FrostWebEntryPoint {
    fun frostJsi(): FrostJSI
}

fun interface FrostEmitter<T> : (T) -> Unit

fun <T> MutableSharedFlow<T>.asFrostEmitter(): FrostEmitter<T> = FrostEmitter { tryEmit(it) }

@Module
@InstallIn(FrostWebComponent::class)
object FrostWebFlowModule {
    @Provides
    @FrostWebScoped
    @FrostRefresh
    fun refreshFlow(parent: FrostContentParent): SharedFlow<Boolean> = parent.refreshFlow

    @Provides
    @FrostWebScoped
    @FrostRefresh
    fun refreshEmit(parent: FrostContentParent): FrostEmitter<Boolean> = parent.refreshEmit
}

/**
 * Observable to get data on whether view is refreshing or not
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FrostRefresh
