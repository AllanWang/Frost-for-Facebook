/*
 * Copyright 2020 Allan Wang
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

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import ca.allanwang.kau.kpref.KPrefFactory
import ca.allanwang.kau.kpref.KPrefFactoryInMemory
import com.pitchedapps.frost.db.FrostDatabase
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.prefs.Prefs
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get
import org.koin.core.module.Module
import org.koin.dsl.module

class FrostTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, FrostTestApp::class.java.name, context)
    }
}

class FrostTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement(), KoinComponent {
            override fun evaluate() {

                // Reset prefs
                get<Prefs>().reset()

                base.evaluate()
            }
        }
}

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
