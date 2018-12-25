package com.pitchedapps.frost.activities

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.runner.RunWith
import android.content.Intent
import com.pitchedapps.frost.utils.ARG_COOKIE
import com.pitchedapps.frost.utils.ARG_IMAGE_URL
import com.pitchedapps.frost.utils.ARG_TEXT
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class ImageActivityTest {

    @get:Rule
    val activity: ActivityTestRule<ImageActivity> = ActivityTestRule(ImageActivity::class.java, true, false)

    private fun launchActivity(imageUrl: String, text: String? = null, cookie: String? = null) {
        val intent = Intent().apply {
            putExtra(ARG_IMAGE_URL, imageUrl)
            putExtra(ARG_TEXT, text)
            putExtra(ARG_COOKIE, cookie)
        }
        activity.launchActivity(intent)
    }

    @Test
    fun intent() {

    }
}