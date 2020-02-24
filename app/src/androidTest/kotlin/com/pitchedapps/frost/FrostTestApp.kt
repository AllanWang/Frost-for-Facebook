package com.pitchedapps.frost

import android.app.Application
import ca.allanwang.kau.kpref.KPrefFactory
import ca.allanwang.kau.kpref.KPrefFactoryInMemory
import com.pitchedapps.frost.db.FrostDatabase
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.Showcase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

class FrostTestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@FrostTestApp)
            modules(
                listOf(
                    FrostDatabase.module(),
                    prefFactoryModule(),
                    Prefs.module(),
                    Showcase.module(),
                    FbCookie.module()
                )
            )
        }
    }

    companion object {
        fun prefFactoryModule(): Module = module {
            single<KPrefFactory> {
                KPrefFactoryInMemory
            }
        }
    }
}