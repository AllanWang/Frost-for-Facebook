package com.pitchedapps.frost

import ca.allanwang.kau.kpref.KPrefFactory
import ca.allanwang.kau.kpref.KPrefFactoryInMemory
import com.pitchedapps.frost.prefs.PrefFactoryModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PrefFactoryModule::class]
)
object PrefFactoryTestModule {
    @Provides
    fun factory(): KPrefFactory = KPrefFactoryInMemory
}
