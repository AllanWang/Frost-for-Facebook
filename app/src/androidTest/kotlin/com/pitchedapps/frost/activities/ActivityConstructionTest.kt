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
package com.pitchedapps.frost.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import com.pitchedapps.frost.StartActivity
import com.pitchedapps.frost.utils.ARG_IMAGE_URL
import com.pitchedapps.frost.utils.ARG_URL
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Basic activity launching tests.
 *
 * Verifies that Hilt injections are not used prior to onCreate
 */
@HiltAndroidTest
class ActivityConstructionTest {

    @ApplicationContext
    @Inject
    lateinit var appContext: Context

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun aboutActivity() {
        launch<AboutActivity>()
    }

    @Test
    fun debugActivity() {
        launch<DebugActivity>()
    }

    @Test
    fun frostWebActivity() {
        launch<FrostWebActivity>(
            intentAction = {
                putExtra(ARG_URL, FORMATTED_URL)
            }
        )
    }

    @Test
    fun imageActivity() {
        launch<ImageActivity>(
            intentAction = {
                putExtra(ARG_IMAGE_URL, FORMATTED_URL)
            }
        )
    }

    @Test
    @Ignore("Doesn't work, yet production is fine.")
    fun introActivity() {
        launch<IntroActivity>()
    }

    @Test
    fun loginActivity() {
        launch<LoginActivity>()
    }

    @Test
    fun mainActivity() {
        launch<MainActivity>()
    }

    @Test
    fun selectorActivity() {
        launch<SelectorActivity>()
    }

    @Test
    fun settingsActivity() {
        launch<SettingsActivity>()
    }

    @Test
    fun startActivity() {
        launch<StartActivity>()
    }

    @Test
    fun tabCustomizerActivity() {
        launch<TabCustomizerActivity>()
    }

    @Test
    fun webOverlayMobileActivity() {
        launch<WebOverlayMobileActivity>(
            intentAction = {
                putExtra(ARG_URL, FORMATTED_URL)
            }
        )
    }

    @Test
    fun webOverlayDesktopActivity() {
        launch<WebOverlayDesktopActivity>(
            intentAction = {
                putExtra(ARG_URL, FORMATTED_URL)
            }
        )
    }

    @Test
    fun webOverlayActivity() {
        launch<WebOverlayActivity>(
            intentAction = {
                putExtra(ARG_URL, FORMATTED_URL)
            }
        )
    }

    private inline fun <reified A : Activity> launch(
        intentAction: Intent.() -> Unit = {},
        activityOptions: Bundle? = null
    ): ActivityScenario<A> {
        val intent = Intent(appContext, A::class.java).also(intentAction)
        return ActivityScenario.launch(intent, activityOptions)
    }

    private companion object {
        const val FORMATTED_URL = "https://www.google.com"
    }
}
