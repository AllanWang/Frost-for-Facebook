package com.pitchedapps.frost.hilt

import com.pitchedapps.frost.components.Core
import com.pitchedapps.frost.components.UseCases
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Components for Mozilla
 *
 * Modelled off of Focus:
 * https://github.com/mozilla-mobile/focus-android/blob/main/app/src/main/java/org/mozilla/focus/Components.kt
 * but with hilt
 */
@Singleton
class FrostComponents @Inject internal constructor(val core: Core, val useCases: UseCases)
