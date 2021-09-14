package com.pitchedapps.frost.activities

import android.app.Activity
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ActivityConstructionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun imageActivity() {
        launch<ImageActivity>()
    }

    private inline fun <reified A : Activity> launch(activityOptions: Bundle? = null) =
        ActivityScenario.launch(A::class.java, activityOptions)
}